package com.shopiq.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import woven.ReferenceProduct;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api")
public class ItemController {

    @Autowired
    private ItemJDBCRepository itemRepository;
    private Gson gson = new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
    private Gson gsonC = new GsonBuilder().disableHtmlEscaping().create();
    private Scraper scraper = new Scraper();
    private JMDBConnector conn = new JMDBConnector();

    @GetMapping("/item/{barcode}")
    public String getItem(@PathVariable String barcode) {
        // todo make scrape asynchronous
        System.out.println("\nitem query: " + barcode);
        if (conn.productExists(barcode)) { // product already in database
            System.out.println("Datasource: mongodb");
            String product = conn.getProduct(barcode);
            return gson.toJson(product);
        } else {
            try {
                ReferenceProduct queryResp = scraper.queryProductInfo(barcode);
                if (queryResp.getStatusCode() != 200) { // scrape unsuccessful
                    System.out.println("[INFO] Scrape unsuccessful");
                    Product newProduct = new Product();
                    newProduct.setStatusCode(505);
                    return gsonC.toJson(newProduct);
                }
                // scrape successful
                // return the product if in database
                if (queryResp.getUpc() != null && queryResp.getUpc() != "" && conn.productExists(queryResp.getUpc())) {
                    System.out.println("fixed query: " + queryResp.getUpc());
                    System.out.println("[INFO] Scrape fixed barcode");
                    return conn.getProduct(queryResp.getUpc());
                } else { // item not inserted into DB, add it, return scraped result
                    conn.insertJsonDocument(gson.toJson(queryResp));
                    return gson.toJson(queryResp);
                }
            } catch (Exception e) {
                e.printStackTrace(); // todo write errors to log file
                ReferenceProduct newProduct = new ReferenceProduct();
                newProduct.setStatusCode(500); // internal server error code
                return gsonC.toJson(newProduct);
            }
        }
    }

    @GetMapping("/create/item")
    public String createItem(@RequestParam(value = "name") String name, @RequestParam(value = "barcode") String barcode) {
        Item aItem = new Item(name, Integer.parseInt(barcode));
        System.out.println(String.format("insert into items_table (name, barcode, barcode_type, stores) values(%s, %d, %d, %s)", aItem.getName(), aItem.getBarcode(), aItem.getBarcodeType(), aItem.getStores()));
        int status = itemRepository.insert(aItem);
        return ""+status;
    }

    @GetMapping("/remove/item-by-barcode")
    public String removeItemByName(@RequestParam(value = "barcode") String barcode) {
        int status = itemRepository.removeItem(Integer.parseInt(barcode));
        return ""+status;
    }

    @GetMapping("/remove/item-by-name")
    public String removeItemByBarcode(@RequestParam(value = "name") String name) {
        int status = itemRepository.removeItem(name);
        return ""+status;
    }

    @GetMapping("/all-items")
    public String getAllItems() {
        List<Item> allItems = itemRepository.findAll();
        String json = new Gson().toJson(allItems);
        return json;
    }

    @PostMapping(path= "/item", consumes = "application/json", produces = "application/json")
    public Item createNewItem(@RequestBody Item item) {
        item.setBarcodeType(item.determineBarcodeType());
        return item;
    }





}

