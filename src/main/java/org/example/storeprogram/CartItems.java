package org.example.storeprogram;

public class CartItems {
    int ID;
    String barcode;
    String productName;
    int quantity;
    double price;


    CartItems(int ID,String barcode, String productName, int quantity,double price) {
        this.ID = ID;
        this.barcode = barcode;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
}
