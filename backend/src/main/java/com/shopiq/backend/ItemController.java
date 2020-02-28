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

    @GetMapping("/item")
    public String getItem(@RequestParam(value = "barcode") String barcode) {
        // todo make this request asynchronous
        // todo add the product product to the database when the scan has been kicked off

        // todo check if barcode already exists in the database
        if (conn.productExists(barcode)) {
            String product = conn.getProduct(barcode);
            System.out.println("product already added!");
            return gsonC.toJson(product);
        } else {
            try {
                Product queryResp = scraper.queryProductInfo(barcode);
                if (queryResp.getStatusCode() != 200) {
                    Product newProduct = new Product();
                    newProduct.setStatusCode(505);
                    // todo emit this product code
                } else { // todo the scrape worked
                    // insert item into to database for future reference
                    conn.insertJsonDocument(gson.toJson(queryResp));
                }
                // todo use unfinished timer to identify slower scrape sessions
                if (conn.productExists(barcode)) { // scrape was successful
                    return conn.getProduct(barcode);
                } else { // error when scraping information
                    Product newProduct = new Product();
                    newProduct.setStatusCode(505);
                    return gsonC.toJson(newProduct);
                }
            } catch (Exception e) {
                e.printStackTrace(); // todo record error log and the code that caused the errorm
                Product newProduct = new Product();
                newProduct.setStatusCode(505);
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

