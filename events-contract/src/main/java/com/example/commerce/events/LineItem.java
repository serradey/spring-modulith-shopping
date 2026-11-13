package com.example.commerce.events;

/** A single line of a cart/order: an item and its quantity. */
public record LineItem(String itemId, int quantity) {}
