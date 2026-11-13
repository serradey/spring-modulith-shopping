package com.example.shopping.cart;

public record ItemRemoved(String cartId, String itemId, int removedQuantity) implements CartEvent {}
