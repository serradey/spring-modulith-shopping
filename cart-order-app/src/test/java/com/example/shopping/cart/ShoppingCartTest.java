package com.example.shopping.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Unit tests for the cart aggregate rules from docs/requirements/shopping-cart.feature. */
class ShoppingCartTest {

    private ShoppingCart newCart() {
        return ShoppingCart.newCart(CartId.of("cart-1"));
    }

    @Test
    void addsItemToEmptyCart() {
        ShoppingCart cart = newCart();
        ItemAdded event = cart.addItem("socks", 3);

        assertThat(event.itemId()).isEqualTo("socks");
        assertThat(event.quantity()).isEqualTo(3);
        assertThat(cart.itemsAsMap()).containsEntry("socks", 3);
    }

    @Test
    void rejectsAddingAnItemAlreadyInTheCart() {
        ShoppingCart cart = newCart();
        cart.addItem("socks", 3);

        assertThatThrownBy(() -> cart.addItem("socks", 2))
                .isInstanceOf(CartException.class)
                .hasMessageContaining("already added");
    }

    @Test
    void rejectsAddingWithNonPositiveQuantity() {
        ShoppingCart cart = newCart();
        assertThatThrownBy(() -> cart.addItem("socks", 0))
                .isInstanceOf(CartException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void adjustsQuantityOfExistingItem() {
        ShoppingCart cart = newCart();
        cart.addItem("socks", 3);

        ItemQuantityAdjusted event = cart.adjustItem("socks", 5);

        assertThat(event.oldQuantity()).isEqualTo(3);
        assertThat(event.newQuantity()).isEqualTo(5);
        assertThat(cart.itemsAsMap()).containsEntry("socks", 5);
    }

    @Test
    void rejectsAdjustingItemNotInCart() {
        ShoppingCart cart = newCart();
        assertThatThrownBy(() -> cart.adjustItem("hat", 2))
                .isInstanceOf(CartException.class)
                .hasMessageContaining("not present");
    }

    @Test
    void removesPresentItem() {
        ShoppingCart cart = newCart();
        cart.addItem("socks", 3);

        Optional<ItemRemoved> event = cart.removeItem("socks");

        assertThat(event).isPresent();
        assertThat(event.get().removedQuantity()).isEqualTo(3);
        assertThat(cart.itemsAsMap()).doesNotContainKey("socks");
    }

    @Test
    void removingAbsentItemIsANoOp() {
        ShoppingCart cart = newCart();
        Optional<ItemRemoved> event = cart.removeItem("hat");
        assertThat(event).isEmpty();
    }

    @Test
    void rejectsCheckoutOfEmptyCart() {
        ShoppingCart cart = newCart();
        assertThatThrownBy(cart::checkout)
                .isInstanceOf(CartException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void checksOutNonEmptyCart() {
        ShoppingCart cart = newCart();
        cart.addItem("socks", 3);

        CartCheckedOut event = cart.checkout();

        assertThat(cart.isCheckedOut()).isTrue();
        assertThat(event.items()).containsEntry("socks", 3);
    }

    @Test
    void rejectsModificationsAfterCheckout() {
        ShoppingCart cart = newCart();
        cart.addItem("socks", 3);
        cart.checkout();

        assertThatThrownBy(() -> cart.addItem("hat", 1)).isInstanceOf(CartException.class);
        assertThatThrownBy(() -> cart.adjustItem("socks", 1)).isInstanceOf(CartException.class);
        assertThatThrownBy(() -> cart.removeItem("socks")).isInstanceOf(CartException.class);
        assertThatThrownBy(cart::checkout).isInstanceOf(CartException.class);
    }
}
