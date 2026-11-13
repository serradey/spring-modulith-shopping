package com.example.commerce.events;

import java.time.Instant;
import java.util.UUID;

public record ItemRemovedEvent(UUID eventId, String cartId, String itemId, int removedQuantity, Instant occurredAt)
        implements CartEventMessage {

    @Override
    public String eventType() {
        return "ItemRemoved";
    }
}
