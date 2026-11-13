package com.example.shopping.order;

import com.example.shopping.order.internal.FulfillmentRequest;
import com.example.shopping.order.internal.OrderFulfillmentPort;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates orders from checked-out carts and submits them to the external fulfillment service. */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orders;
    private final OrderFulfillmentPort fulfillment;

    OrderService(OrderRepository orders, OrderFulfillmentPort fulfillment) {
        this.orders = orders;
        this.fulfillment = fulfillment;
    }

    /**
     * Creates an order for a checked-out cart. Idempotent per cart: a repeated checkout
     * notification (at-least-once delivery) does not create a second order.
     */
    @Transactional
    public void createFromCheckout(String cartId, Map<String, Integer> items) {
        if (orders.existsByCartId(cartId)) {
            log.info("Order for cart {} already exists; ignoring duplicate checkout.", cartId);
            return;
        }
        Order order = Order.fromCart(cartId, items);
        orders.save(order);

        var lines = items.entrySet().stream()
                .map(e -> new FulfillmentRequest.FulfillmentLine(e.getKey(), e.getValue()))
                .toList();
        fulfillment.submit(new FulfillmentRequest(cartId, lines));
        log.info("Created order {} for cart {} ({} units).", order.getId(), cartId, order.totalUnits());
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByCartId(String cartId) {
        return orders.findByCartId(cartId);
    }
}
