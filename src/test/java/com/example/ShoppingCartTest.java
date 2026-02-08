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
}
