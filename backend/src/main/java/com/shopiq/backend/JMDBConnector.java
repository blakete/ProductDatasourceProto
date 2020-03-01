package com.shopiq.backend;

import com.mongodb.*;
import com.mongodb.util.JSON;

public class JMDBConnector {

    MongoClient mongoClient;
    DB database;
    DBCollection collection;


    public JMDBConnector() {
        try {
            mongoClient = new MongoClient("localhost", 27017);
            establishConnection();
            createCollection();
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

    public boolean createCollection() {
        database.createCollection("products", null);
        collection = database.getCollection("products");
        return true;
    }

    public boolean createDocument() {
        BasicDBObject document = new BasicDBObject();
        document.put("name", "Product_1234");
        document.put("barcode", "1234");
        collection.insert(document);
        return true;
    }

    public boolean readDocument() {
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", "Product_1234");
        DBCursor cursor = collection.find(searchQuery);
        while (cursor.hasNext()) {
            System.out.println(cursor.next());
        }
        return true;
    }

    public boolean insertJsonDocument(String json) {
        DBObject dbObject = (DBObject) JSON.parse(json);
        collection.insert(dbObject);
        return true;
    }

    public String getProduct(String barcode) {
        DBObject query = BasicDBObjectBuilder.start().add("upc", barcode).get();
        DBCursor cursor = collection.find(query);
        String productDocument = cursor.next().toString();
//        while(cursor.hasNext()) {
//            productDocument = cursor.next().toString();
//        }
        return productDocument;
    }

    public boolean productExists(String barcode) {
        DBObject query = BasicDBObjectBuilder.start().add("upc", barcode).get();
        DBCursor cursor = collection.find(query);
//        while(cursor.hasNext()){
//            System.out.println(cursor.next());
//        }
        return cursor.hasNext();
    }


}