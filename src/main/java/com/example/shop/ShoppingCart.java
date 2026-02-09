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
    private int percentageDiscount = 0;
    private int fixedDiscount = 0;

    public int getTotal() {
        int total = 0;
        for (Line line : cartItem.values()) {
            total += line.price * line.quantity;
        }
        if (percentageDiscount > 0) {
            total = total * (100 - percentageDiscount) / 100;
        }
        if (fixedDiscount > 0) {
            total -= fixedDiscount;
        }
        return Math.max(total, 0);
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

    public void removeItem(String milk) {
        cartItem.remove(milk);
    }

    public void applyPercentageDiscount(int discount) {
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Discount must be greater than or equal to 0");
        }
        this.percentageDiscount = discount;
    }

    public void applyFixedDiscount(int discountAmount) {
        if (discountAmount < 0) {
            throw new IllegalArgumentException("Discount amount must be greater than or equal to 0");
        }
        this.fixedDiscount = discountAmount;
    }
}
