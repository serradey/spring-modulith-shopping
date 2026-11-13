package com.example.shopping.cart;

import com.example.commerce.events.CartCheckedOutEvent;
import com.example.commerce.events.ItemAddedEvent;
import com.example.commerce.events.ItemQuantityAdjustedEvent;
import com.example.commerce.events.ItemRemovedEvent;
import com.example.commerce.events.LineItem;
import com.example.commerce.events.ShoppingTopics;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;

/**
 * Externalizes cart domain events to Kafka for the Analytics application. Each cart event is routed
 * to the shared {@code shopping-cart-events} topic (keyed by cart id, for per-cart ordering) and
 * mapped to the framework-neutral integration event defined in the {@code events-contract} module.
 */
@Configuration
class CartEventExternalization {

    @Bean
    EventExternalizationConfiguration eventExternalizationConfiguration() {
        return EventExternalizationConfiguration.externalizing()
                .select(event -> event instanceof CartEvent)
                .route(ItemAdded.class, CartEventExternalization::route)
                .route(ItemQuantityAdjusted.class, CartEventExternalization::route)
                .route(ItemRemoved.class, CartEventExternalization::route)
                .route(CartCheckedOut.class, CartEventExternalization::route)
                .mapping(
                        ItemAdded.class,
                        e -> new ItemAddedEvent(UUID.randomUUID(), e.cartId(), e.itemId(), e.quantity(), Instant.now()))
                .mapping(
                        ItemQuantityAdjusted.class,
                        e -> new ItemQuantityAdjustedEvent(
                                UUID.randomUUID(),
                                e.cartId(),
                                e.itemId(),
                                e.oldQuantity(),
                                e.newQuantity(),
                                Instant.now()))
                .mapping(
                        ItemRemoved.class,
                        e -> new ItemRemovedEvent(
                                UUID.randomUUID(), e.cartId(), e.itemId(), e.removedQuantity(), Instant.now()))
                .mapping(
                        CartCheckedOut.class,
                        e -> new CartCheckedOutEvent(
                                UUID.randomUUID(), e.cartId(), toLineItems(e.items()), e.checkoutTime()))
                .build();
    }

    private static RoutingTarget route(CartEvent event) {
        return RoutingTarget.forTarget(ShoppingTopics.CART_EVENTS).andKey(event.cartId());
    }

    private static List<LineItem> toLineItems(Map<String, Integer> items) {
        return items.entrySet().stream()
                .map(entry -> new LineItem(entry.getKey(), entry.getValue()))
                .toList();
    }
}
