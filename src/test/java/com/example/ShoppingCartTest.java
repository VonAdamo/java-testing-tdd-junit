package com.example;

import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ShoppingCartTest {

    @Test
    void totalPriceIsZeroForEmptyCart() {
        ShoppingCart cart = new ShoppingCart();

        assertThat(cart.getTotal()).isZero();
    }

    @Test
    void totalPriceIncreasesWhenAddingItem() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("milk", 190);

        assertThat(cart.getTotal()).isEqualTo(190);
    }
}
