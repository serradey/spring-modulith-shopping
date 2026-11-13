package com.example.commerce.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CartCheckedOutEvent(UUID eventId, String cartId, List<LineItem> items, Instant occurredAt)
        implements CartEventMessage {

    @Override
    public String eventType() {
        return "CartCheckedOut";
    }
}
