package com.example.shopping.order.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * In-process stand-in for the external order/fulfillment service. Mirrors the reference Akka sample:
 * it tallies the units, logs the submission and acknowledges. Replace with a real client (REST,
 * messaging, gRPC) to integrate an actual fulfillment system.
 */
@Component
class StubOrderFulfillmentAdapter implements OrderFulfillmentPort {

    private static final Logger log = LoggerFactory.getLogger(StubOrderFulfillmentAdapter.class);

    @Override
    public FulfillmentResult submit(FulfillmentRequest request) {
        int totalUnits = request.items().stream()
                .mapToInt(FulfillmentRequest.FulfillmentLine::quantity)
                .sum();
        log.info("Order {} items from cart {}.", totalUnits, request.cartId());
        return FulfillmentResult.ok();
    }
}
