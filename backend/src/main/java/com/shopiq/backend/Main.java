package com.shopiq.backend;
import com.mongodb.util.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class Main {
    public static final String[] barCodes = {"0885909950805", "075500000010", "075800000010"};
    public static void main(String[] args)
    {
        Scraper scraper = new Scraper();
        for(String bCode : barCodes){
            Product newProduct = scraper.queryProductInfo(bCode);
            if(newProduct.getStatusCode()==200)
                System.out.println(newProduct.toString());
        }
    }
}
