package com.example.shopping.order.internal;

/** External-system response to an order submission. */
public record FulfillmentResult(boolean accepted) {

    public static FulfillmentResult ok() {
        return new FulfillmentResult(true);
    }
}
