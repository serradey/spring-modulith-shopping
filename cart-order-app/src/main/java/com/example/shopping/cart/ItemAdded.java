package com.example.shopping.cart;

public record ItemAdded(String cartId, String itemId, int quantity) implements CartEvent {}
