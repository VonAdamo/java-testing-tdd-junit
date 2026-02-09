package com.example;

import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ShoppingCartTest {

    @Test
    void totalIsZeroForEmptyCart() {
        ShoppingCart cart = new ShoppingCart();

        assertThat(cart.getTotal()).isZero();
    }

    @Test
    void totalIncreasesWhenAddingItem() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("milk", 190);

        assertThat(cart.getTotal()).isEqualTo(190);
    }

    @Test
    void totalCountsSameProductTwiceWhenAddedTwice() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("milk", 190);
        cart.addItem("milk", 190);

        assertThat(cart.getTotal()).isEqualTo(380);
    }

    @Test
    void updateQuantityChangesTotalForExistingItem() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("milk", 190);
        cart.updateQuantity("milk", 3);

        assertThat(cart.getTotal()).isEqualTo(570);
    }

    @Test
    void updateQuantityToZeroRemovesItemFromCart() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("milk", 190);
        cart.updateQuantity("milk", 0);

        assertThat(cart.getTotal()).isZero();
    }
}
