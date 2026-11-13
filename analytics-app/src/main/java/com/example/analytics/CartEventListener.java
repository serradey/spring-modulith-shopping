package com.example.analytics;

import com.example.commerce.events.CartEventMessage;
import com.example.commerce.events.ShoppingTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Subscribes to the cart event stream on Kafka. The value deserializer reconstructs the concrete
 * {@link CartEventMessage} (via the type header), and processing is delegated to the analytics
 * service. Offsets are committed after successful processing, so consumption resumes from the last
 * processed position after a restart.
 */
@Component
class CartEventListener {

    private final AnalyticsService analytics;

    CartEventListener(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    @KafkaListener(topics = ShoppingTopics.CART_EVENTS, groupId = "${analytics.consumer-group:analytics}")
    void on(CartEventMessage event) {
        analytics.record(event);
    }
}
