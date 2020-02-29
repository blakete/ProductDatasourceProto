package com.shopiq.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Scraper {

    //    private Gson gson = new Gson();
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public Product queryProductInfo(String barcode) {
        Product newProduct = this.queryBCLookup(barcode);
        if(newProduct.getStatusCode()!=200){
            newProduct = this.queryUPCItemDB(barcode);
        }
        return newProduct;
    }
    private Product queryBCLookup(String barcode) {
        // Here we create a document object and use JSoup to fetch the website
        Product newProduct = new Product();
        try {
            String lookup_url = "https://www.barcodelookup.com/";
            Document doc = Jsoup.connect(lookup_url+barcode).get();
            if (!isProductPresent(doc))
            {
                ErrorWriter.logError("[ERROR] Malformed barcode: " + barcode);
                newProduct.setStatusCode(404); // status 404
                return newProduct;
            }
            String name = doc.select("div.product-details > h4").text();
            String category = doc.select("div.product-details > div.row > div.col-md-12 > div.row > div.col-md-12 > div.product-text-label > span.product-text").first().text();
            String manufacturer = "";
            try {
                manufacturer = doc.select("div.product-details > div.row > div.col-md-12 > div.row > div.col-md-12 > div.product-text-label > span.product-text").get(1).text();
            }catch (Exception e)
            {
                manufacturer = "N/A";
            }
            String pictureURL = doc.select("div.col-md-6 > #wrapper > #images > img").first().absUrl("src");
            String[] codes = parseCodes(doc.select("div.product-details > div.row > div.col-md-12 > div.product-text-label").text());
            // parse through codes and add specific document entries for easy product existence queries to the database
            for (int i = 0; i<codes.length; i++) {
                if (codes[i].contains("UPC")) {
                    newProduct.setUpc(codes[i].substring(4));
                } else if (codes[i].contains("EAN")) {
                    newProduct.setEan(codes[i].substring(4));
                }
            }
            newProduct.setName(name);
            newProduct.setCodes(codes);
            newProduct.setCategory(category);
            newProduct.setManufacturer(manufacturer);
            newProduct.setPictureURL(pictureURL); // todo verify the picture is a valid asynchronously
            newProduct.setStatusCode(200); // status 405
            // todo get the url to the image for download
        } catch (Exception e)
        {
            ErrorWriter.logError(e.getMessage());
            newProduct.setStatusCode(405); // status 405
            return newProduct;
        }
        return newProduct;
    }
    private Product queryUPCItemDB(String barcode) {
        BufferedReader input = null;
        Product newProduct = new Product();
        if(barcode.length()!=12){
            newProduct.setStatusCode(400);
            ErrorWriter.logError("[ERROR] Malformed barcode: " + barcode);
            return newProduct;
        }
        try {
            String lookup_url = "https://api.upcitemdb.com/prod/trial/lookup?upc=";
            HttpURLConnection connection = (HttpURLConnection) new URL(lookup_url+barcode).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            try {
                connection.getInputStream();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    String errorMsg = "Error in queryUPCItemDB\n";
                    newProduct.setStatusCode(400);
                    if(connection.getResponseCode() == 404){
                        errorMsg+="[ERROR] Item not found\n";
                        newProduct.setStatusCode(505);
                    }else if(connection.getResponseCode() == 429)
                    {
                        errorMsg+="[ERROR] Request limit reached for UPCItemDB\n";
                        newProduct.setStatusCode(505);
                    }
                    ErrorWriter.logError(errorMsg + "HTTP Response code: " + connection.getResponseCode() + "\nURL: " + (lookup_url + barcode));
                    return newProduct;
                }
            }catch (Exception e)
            {
                ErrorWriter.logError("Error in queryUPCItemDB: " + e.getMessage());
            }
            URL urlObject = new URL(lookup_url + barcode);
            HttpURLConnection connection2 = (HttpURLConnection) urlObject.openConnection();
            connection2.setReadTimeout(10000);
            input = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
            String inputLine;
            String htmlPageStr = "";
            while ((inputLine = input.readLine()) != null)
                htmlPageStr += inputLine + "\n";
            JSONObject obj = new JSONObject(htmlPageStr);
            String itemString = obj.get("items").toString();
            JSONObject items = new JSONObject(itemString.substring(1, itemString.length() - 1));
            String name = items.getString("title");
            String category = items.getString("category");
            String ean = items.getString("ean");
            String upc = items.getString("upc");
            String manufacturer = items.getString("brand");
            String images = items.get("images").toString();
            images = images.substring(1, images.length() - 1);
            String[] imageUrl = images.split(",");
            String[] codes = {ean, upc};
            String pictureURL = "";
            if (imageUrl.length > 0)
                pictureURL = imageUrl[0];
            newProduct.setName(name);
            newProduct.setCodes(codes);
            newProduct.setCategory(category);
            newProduct.setManufacturer(manufacturer);
            newProduct.setPictureURL(pictureURL); // todo verify the picture is a valid asynchronously
            newProduct.setStatusCode(200); // status 405
        }catch (IOException e){
            newProduct.setStatusCode(405);
            ErrorWriter.logError(e.getMessage());
        }catch (Exception e)
        {
            System.out.println(e.toString());
        }
        return newProduct;
    }


    public String[] parseCodes(String rawInput) {
        String[] codes = rawInput.split(",");
        // remove the label text at beginning of first barcode
        codes[0] = codes[0].substring(17);
        // remove any white space trailing before or after all other codes
        for (int i = 0; i < codes.length; i++)
            codes[i] = codes[i].trim();
        return codes;
    }

    public boolean isProductPresent(Document doc) {
        String notExist = doc.select("section.jumbotron > div.top-container > h1").text();
        String malformedBarcode = doc.select("div.container > div.row > div.col-md-offset-2 > center > h1").text();
        if (malformedBarcode != null && malformedBarcode.contains("is not a valid barcode number")) {
            return false;
        } else if (notExist != null && notExist.contains("Product Doesn't Exist")) {
            ErrorWriter.logError("[ERROR] Product does not exist in this database");
            return false;
        }
        return true;
    }

}
