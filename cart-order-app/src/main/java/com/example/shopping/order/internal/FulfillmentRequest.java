package com.example.shopping.order.internal;

import java.util.List;

/** External-system representation of an order submission (the ACL's outbound model). */
public record FulfillmentRequest(String cartId, List<FulfillmentLine> items) {

    public record FulfillmentLine(String itemId, int quantity) {}
}
