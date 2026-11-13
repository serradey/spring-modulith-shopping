package com.example.shopping.cart;

import java.time.Instant;
import java.util.Map;

/**
 * Published when a cart is finalised. Carries a snapshot of the cart contents (item id to quantity)
 * at the moment of checkout so downstream consumers do not need to call back into the cart.
 */
public record CartCheckedOut(String cartId, Map<String, Integer> items, Instant checkoutTime)
        implements CartEvent {}
