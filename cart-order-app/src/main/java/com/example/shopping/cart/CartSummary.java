package com.example.shopping.cart;

import java.util.Map;

/** A snapshot of a cart: its line items (item id to quantity) and whether it is checked out. */
public record CartSummary(Map<String, Integer> items, boolean checkedOut) {}
