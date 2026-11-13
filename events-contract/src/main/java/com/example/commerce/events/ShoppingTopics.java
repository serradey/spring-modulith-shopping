package com.example.commerce.events;

/**
 * Logical Kafka destinations shared between the publishing application (Cart) and the consuming
 * application (Analytics). Kept free of any framework dependency on purpose.
 */
public final class ShoppingTopics {

    public static final String CART_EVENTS = "shopping-cart-events";

    private ShoppingTopics() {}
}
