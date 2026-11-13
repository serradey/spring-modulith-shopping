package com.example.shopping.cart;

/** Raised when a cart operation violates a business rule (the operation is rejected). */
public class CartException extends RuntimeException {

    public CartException(String message) {
        super(message);
    }
}
