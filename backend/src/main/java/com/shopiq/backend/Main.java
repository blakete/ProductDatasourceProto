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
    public static final String[] barCodes = {"0885909950805", "075500000010", "079400446039", "855380004281", "681131143493", "681131039352",
    "855380004304", "855380004878", "011111062191", "037000786658", "009200300248", "009200293755"};
    public static void main(String[] args)
    {
        Scraper scraper = new Scraper();
        for(String bCode : barCodes){
            Product newProduct = scraper.queryProductInfo(bCode);
            if(newProduct.getStatusCode()==200) {
                System.out.println(newProduct.toString());
                System.out.println();
            }else{
                System.out.println("Unable to find: " + bCode);
                System.out.println(newProduct.getStatusCode());
                System.out.println();
            }
        }
    }
}
