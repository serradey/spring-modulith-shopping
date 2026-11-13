package com.example.shopping.cart;

/** Raised when a cart cannot be found (unknown id, or a cart that never held any items). */
public class CartNotFoundException extends CartException {

    public CartNotFoundException(String cartId) {
        super("Cart '" + cartId + "' not found");
    }
}
