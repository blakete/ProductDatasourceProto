package com.shopiq.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Scraper {

    //    private Gson gson = new Gson();
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public Product queryProductInfo(String barcode) {
        // Here we create a document object and use JSoup to fetch the website
        Product newProduct = new Product();
        try {
            String lookup_url = "https://www.barcodelookup.com/";
            Document doc = Jsoup.connect(lookup_url+barcode).get();
            if (!isProductPresent(doc))
            {
                newProduct.setStatusCode(404); // status 404
                return newProduct;
            }
            String name = doc.select("div.product-details > h4").text();
            String category = doc.select("div.product-details > div.row > div.col-md-12 > div.row > div.col-md-12 > div.product-text-label > span.product-text").first().text();
            String manufacturer = doc.select("div.product-details > div.row > div.col-md-12 > div.row > div.col-md-12 > div.product-text-label > span.product-text").get(1).text();
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
            System.out.print("[ERROR]: ");
            e.printStackTrace();
            newProduct.setStatusCode(405); // status 405
            return newProduct;
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
            System.out.println("[ERROR] Barcode is malformed");
            return false;
        } else if (notExist != null && notExist.contains("Product Doesn't Exist")) {
            System.out.println("[ERROR] Product does not exist in this database");
            return false;
        }
        return true;
    }

}
