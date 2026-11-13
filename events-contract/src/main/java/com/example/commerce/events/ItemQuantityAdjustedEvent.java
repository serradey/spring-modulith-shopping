package com.example.commerce.events;

import java.time.Instant;
import java.util.UUID;

public record ItemQuantityAdjustedEvent(
        UUID eventId, String cartId, String itemId, int oldQuantity, int newQuantity, Instant occurredAt)
        implements CartEventMessage {

    @Override
    public String eventType() {
        return "ItemQuantityAdjusted";
    }
}
