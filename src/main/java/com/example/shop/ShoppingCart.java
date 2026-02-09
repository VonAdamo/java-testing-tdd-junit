package com.example.shop;

public class ShoppingCart {

    private int total;

    public int getTotal() {
        return total;
    }

    public void addItem(String productId, int price) {
        total += price;
    }
}
