package com.example.shopping.cart.internal;

import com.example.shopping.cart.ItemAdded;
import com.example.shopping.cart.ItemQuantityAdjusted;
import com.example.shopping.cart.ItemRemoved;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Keeps the item-popularity read model in sync with cart activity by applying the delta rules from
 * {@code item-popularity.feature}. Runs asynchronously after the cart transaction commits, so the
 * read model is eventually consistent. {@code CartCheckedOut} intentionally has no handler: checkout
 * does not change popularity.
 */
@Component
class ItemPopularityListener {

    private static final Logger log = LoggerFactory.getLogger(ItemPopularityListener.class);

    private final ItemPopularityService service;

    ItemPopularityListener(ItemPopularityService service) {
        this.service = service;
    }

    @ApplicationModuleListener
    void on(ItemAdded event) {
        service.changeBy(event.itemId(), event.quantity());
        log.debug("Item popularity: '{}' += {}", event.itemId(), event.quantity());
    }

    @ApplicationModuleListener
    void on(ItemQuantityAdjusted event) {
        long delta = (long) event.newQuantity() - event.oldQuantity();
        service.changeBy(event.itemId(), delta);
        log.debug("Item popularity: '{}' += {}", event.itemId(), delta);
    }

    @ApplicationModuleListener
    void on(ItemRemoved event) {
        service.changeBy(event.itemId(), -event.removedQuantity());
        log.debug("Item popularity: '{}' -= {}", event.itemId(), event.removedQuantity());
    }
}
