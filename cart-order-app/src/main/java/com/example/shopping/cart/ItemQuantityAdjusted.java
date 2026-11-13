package com.example.shopping.cart;

public record ItemQuantityAdjusted(String cartId, String itemId, int oldQuantity, int newQuantity)
        implements CartEvent {}
