package com.example;

import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void updateQuantityThrowsExceptionWhenQuantityIsNegative() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("milk", 190);

        assertThatThrownBy(() -> cart.updateQuantity("milk", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than or equal to 0");
    }

    @Test
    void removeItemRemovesItemAndUpdatesTotal() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("milk", 190);
        cart.addItem("bread", 250);

        cart.removeItem("milk");

        assertThat(cart.getTotal()).isEqualTo(250);
    }

    @Test
    void removeItemDoesNothingWhenItemDoesNotExist() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("milk", 190);

        cart.removeItem("bread");

        assertThat(cart.getTotal()).isEqualTo(190);
    }

    @Test
    void applyPercentageDiscountReducesTotal() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("milk", 190);
        cart.addItem("bread", 250);

        cart.applyPercentageDiscount(10);

        assertThat(cart.getTotal()).isEqualTo(396);
    }

    @Test
    void applyPercentageDiscountThrowsExceptionWhenPercentIsNegative() {
        ShoppingCart cart = new ShoppingCart();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cart.applyPercentageDiscount(-1)
        );
        assertThat(ex.getMessage()).isEqualTo("Discount must be greater than or equal to 0");
    }
}
