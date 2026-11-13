package com.example.shopping.order.internal;

/**
 * Anti-corruption layer port abstracting the external order/fulfillment service. The order module
 * translates its own domain into the {@link FulfillmentRequest} model expected by the external
 * system, shielding the domain from that system's contract.
 */
public interface OrderFulfillmentPort {

    FulfillmentResult submit(FulfillmentRequest request);
}
