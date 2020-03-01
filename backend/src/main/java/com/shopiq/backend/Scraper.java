package com.shopiq.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Scraper {

    //    private Gson gson = new Gson();
    public static final String imageDirectory = "Images/";
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public Product queryProductInfo(String barcode) {
        Product newProduct = this.queryBCLookup(barcode);
        if(newProduct.getStatusCode()!=200){
            System.out.println("Datasource: UPCItemDB");
            newProduct = this.queryUPCItemDB(barcode);
            return newProduct;
        }
        System.out.println("Datasource: BCLookup");
//        System.out.println(newProduct.toString());
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
            saveImage(pictureURL, imageDirectory, name);
            newProduct.setName(name);
            newProduct.setCodes(codes);
            newProduct.setCategory(category);
            newProduct.setManufacturer(manufacturer);
            newProduct.setPictureURL(pictureURL); // todo verify the picture is a valid asynchronously
            newProduct.setStatusCode(200);
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
        String lookup_url = "https://api.upcitemdb.com/prod/trial/lookup?upc=";
        BufferedReader input = null;
        HttpURLConnection connection = null;
        Product newProduct = new Product();
//        if(barcode.length()!=12){ // This url can only handle UPC barcodes // todo fix errors here when requesting truncated barcode
//            newProduct.setStatusCode(406); // Malformed barcode = 406
//            ErrorWriter.logError("[ERROR] Malformed barcode: " + barcode + "\n" + lookup_url + " expects a UPC barcode");
//            return newProduct;
//        }
        try {
            connection = (HttpURLConnection) new URL(lookup_url+barcode).openConnection(); // Open connection
            connection.setInstanceFollowRedirects(false); // Don't redirect (may not be necessary in this case)
            connection.connect(); // Connect
            connection.getInputStream();
            input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            String htmlPageStr = "";
            while ((inputLine = input.readLine()) != null) // Turn webpage into string object
                htmlPageStr += inputLine + "\n";
            try {
                JSONObject obj = new JSONObject(htmlPageStr); // Turn string into JSON object
                String itemString = obj.get("items").toString(); // Get item subcategory from JSON
                JSONObject items = new JSONObject(itemString.substring(1, itemString.length() - 1)); // Clean and turn into JSON obj
                String name = items.getString("title"); // Self explanatory
                String category = items.getString("category");
                String ean = items.getString("ean");
                String upc = items.getString("upc");
                String manufacturer = items.getString("brand");
                String images = items.get("images").toString();
                images = images.substring(1, images.length() - 1);
                String[] imageUrl = images.split(",");
                String[] codes = {ean, upc};
                String pictureURL = ""; //
                if (imageUrl.length > 0)
                {
                    // Make sure there is at least one picture (can be multiple)
                    if (imageUrl[0].length() > 2) // stupid check to make sure url is not null
                    {
                        pictureURL = imageUrl[0].substring(1);
                        newProduct.setPictureURL(pictureURL); // todo verify the picture is a valid asynchronously
                    }
//                    saveImage(pictureURL, imageDirectory, name); // todo fix errors here when saving image
                }

                newProduct.setName(name);
                newProduct.setCodes(codes);
                newProduct.setCategory(category);
                newProduct.setManufacturer(manufacturer);
                newProduct.setStatusCode(200); // Everything was successful
            }catch(Exception e){ // JSON exception (webpage returned anything other than JSON)
                newProduct.setStatusCode(405);
                ErrorWriter.logError("[ERROR]: " + lookup_url + ", JSON object expected:\n" + e.getMessage());
            }
        }catch (Exception e) // HTTP exception
        {
            int repCode;
            try{ // Attempt to get response code. If unreachable assumed to be 400
                repCode = connection.getResponseCode();
            }catch (Exception ee) {
                repCode = 400;
            }
            if (repCode != HttpURLConnection.HTTP_OK) { // Probably redundant, had to be an exception thrown to be here in the first place
                String errorMsg = "Error in queryUPCItemDB\n";
                newProduct.setStatusCode(400);
                if (repCode == 404) { // Item not found response code
                    errorMsg += "\tItem not found\n";
                    newProduct.setStatusCode(505);
                } else if (repCode == 429) { // Too many requests response code
                    errorMsg += "\tRequest limit reached for UPCItemDB\n";
                    newProduct.setStatusCode(505);
                }
                ErrorWriter.logError(errorMsg + "\tHTTP Response code: " + repCode + "\n\tURL: " + (lookup_url + barcode));
                return newProduct;
            }
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

    public int saveImage(String imageUrl, String destinationFolder, String itemName){
        if(imageUrl.equals(""))
            return 2;
        String imgName = itemName;
        if(itemName.length()>8)
            imgName = itemName.substring(0,7);
        try {
            String[] baseImage = imageUrl.split("\\?");
            if(baseImage.length>0)
                imageUrl = baseImage[0];
            String path=destinationFolder + imgName + ".jpg";
            path = path.replace(" ", "_");
            URL url = new URL(imageUrl);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(path);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1)
                os.write(b, 0, length);
            is.close();
            os.close();
            return 0;
        }catch (IOException e){
            ErrorWriter.logError("Error saving image:\n\t" + e.getMessage());
        }
        return 1;
    }

}
