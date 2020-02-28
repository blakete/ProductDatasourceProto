package com.shopiq.backend;

import org.hibernate.annotations.Entity;

import java.util.HashMap;


@Entity
public class Item {

    private String name;
    private String stores;
    private int barcode;
    private int barcodeType;
    /*
      Barcode type:
          1. UPC-A
          2. EAN-13
          3. UPC-E
          4. EAN-8
     */
    public Item() {

    }

    public Item(String name, int bc)
    {
        this.name = name;
        this.barcode = bc;
        this.barcodeType = determineBarcodeType();
        this.stores = "";
    }

    public Item(String name, int bc, String strs)
    {
        this.name = name;
        this.barcode = bc;
        this.barcodeType = determineBarcodeType();
        this.stores = strs;
    }

    public int determineBarcodeType()
    {
        HashMap<Integer, Integer> barcodeTypeMap = new HashMap<Integer, Integer>() {{
            put(12, 1);
            put(13, 2);
            put(7, 3);
            put(8, 4);
        }};
        if (barcodeTypeMap.containsKey(Integer.toString(barcode).length()))
        {
            return barcodeTypeMap.get(Integer.toString(barcode).length());
        }
        return -1;
    }

    public String getName() {
        return name;
    }

    public int getBarcode() {
        return barcode;
    }

    public int getBarcodeType() {
        return barcodeType;
    }

    public String getStores() {
        return stores;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBarcode(int barcode) {
        this.barcode = barcode;
    }

    public void setBarcodeType(int barcodeType) {
        this.barcodeType = barcodeType;
    }

    public void setStores(String stores) {
        this.stores = stores;
    }

    public String toString()
    {
        return name + " " + stores + " " + barcode + " " + barcodeType;
    }

}
