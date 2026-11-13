package com.example.shopping.cart;

import org.jmolecules.event.types.DomainEvent;

/**
 * Common supertype for everything that can happen to a cart. These are the cart module's published
 * domain events: in-process listeners (item popularity, order creation) consume them, and they are
 * externalized to Kafka for the Analytics application.
 */
public sealed interface CartEvent extends DomainEvent
        permits ItemAdded, ItemQuantityAdjusted, ItemRemoved, CartCheckedOut {

    String cartId();
}
