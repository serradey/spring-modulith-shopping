package com.example.shopping.order;

import com.example.shopping.cart.CartCheckedOut;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Bridges the Cart and Order modules. Reacts to the in-process {@code CartCheckedOut} domain event
 * (delivered reliably via the Event Publication Registry) and creates the corresponding order in a
 * separate transaction.
 */
@Component
class CheckoutListener {

    private final OrderService orderService;

    CheckoutListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @ApplicationModuleListener
    void on(CartCheckedOut event) {
        orderService.createFromCheckout(event.cartId(), event.items());
    }
}
