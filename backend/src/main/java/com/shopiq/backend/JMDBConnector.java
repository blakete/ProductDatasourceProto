package com.shopiq.backend;

import com.mongodb.*;
import com.mongodb.util.JSON;

public class JMDBConnector {

    MongoClient mongoClient;
    DB database;
    DBCollection referenceProductCollection;
    DBCollection storeCollection;
    DBCollection ownerCollection;
    DBCollection managerCollection;
    DBCollection storeProductCollection;
    DBCollection storeProductHistoryCollection;


    public JMDBConnector() {
        try {
            mongoClient = new MongoClient("localhost", 27017);
            establishConnection();
            createCollections();
//            createDocument();
//            readDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean establishConnection() {
        database = mongoClient.getDB("wovenDB");
        if (database != null)
            return true;
        return false;
    }

    public boolean createCollections() {
        database.createCollection("referenceProduct", null);
        referenceProductCollection = database.getCollection("referenceProduct");

        database.createCollection("store", null);
        storeCollection = database.getCollection("store");

        database.createCollection("owner", null);
        ownerCollection = database.getCollection("owner");

        database.createCollection("manager", null);
        managerCollection = database.getCollection("manager");

        database.createCollection("storeProduct", null);
        storeProductCollection = database.getCollection("storeProduct");

        database.createCollection("storeProductHistory", null);
        storeProductHistoryCollection = database.getCollection("storeProductHistory");

        return true;
    }

    public boolean createDocument() {
        BasicDBObject document = new BasicDBObject();
        document.put("name", "Product_1234");
        document.put("barcode", "1234");
        referenceProductCollection.insert(document);
        return true;
    }

    public boolean readDocument() {
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", "Product_1234");
        DBCursor cursor = referenceProductCollection.find(searchQuery);
        while (cursor.hasNext()) {
            System.out.println(cursor.next());
        }
        return true;
    }

    public boolean insertJsonDocument(String json) {
        System.out.println("[INFO] Inserting document in the database");
        DBObject dbObject = (DBObject) JSON.parse(json);
        referenceProductCollection.insert(dbObject);
        return true;
    }

    public String getProduct(String barcode) {
        DBObject query = BasicDBObjectBuilder.start().add("upc", barcode).get();
        DBCursor cursor = referenceProductCollection.find(query);
        String productDocument = cursor.next().toString();
//        while(cursor.hasNext()) {
//            productDocument = cursor.next().toString();
//        }
        return productDocument;
    }

    public boolean productExists(String barcode) {
        DBObject query = BasicDBObjectBuilder.start().add("upc", barcode).get();
        DBCursor cursor = referenceProductCollection.find(query);
//        while(cursor.hasNext()){
//            System.out.println(cursor.next());
//        }
        return cursor.hasNext();
    }


}