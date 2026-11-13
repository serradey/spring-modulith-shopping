package com.example.commerce.events;

import java.time.Instant;
import java.util.UUID;

public record ItemAddedEvent(UUID eventId, String cartId, String itemId, int quantity, Instant occurredAt)
        implements CartEventMessage {

    @Override
    public String eventType() {
        return "ItemAdded";
    }
}
