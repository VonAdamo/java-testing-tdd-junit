package com.example.shop;

import java.util.HashMap;
import java.util.Map;


public class ShoppingCart {

    private static class Line {
        int price;
        int quantity;

        Line(int price, int quantity) {
            this.price = price;
            this.quantity = quantity;
        }
    }

    private final Map<String, Line> cartItem = new HashMap<>();

    public int getTotal() {
        int total = 0;
        for (Line line : cartItem.values()) {
            total += line.price * line.quantity;
        }
        return total;
    }

    public void addItem(String productId, int price) {
        Line line = cartItem.get(productId);
        if (line == null) {
            cartItem.put(productId, new Line(price,1));
        } else {
            line.quantity += 1;
        }
    }

    public void updateQuantity(String productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be greater than or equal to 0");
        }
        Line line = cartItem.get(productId);
        if (line == null) return;
        if (quantity == 0) {
            cartItem.remove(productId);
        } else {
            line.quantity = quantity;
        }
    }
}
