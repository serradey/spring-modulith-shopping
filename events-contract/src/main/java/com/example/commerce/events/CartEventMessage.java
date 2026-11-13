package com.example.commerce.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.UUID;

/**
 * Published contract for everything that happens to a cart. This is the single source of truth for
 * the payloads exchanged over Kafka between the Cart context (producer) and the Analytics context
 * (consumer). Plain Java records, no framework coupling.
 *
 * <p>Every message carries a unique {@link #eventId()} (used by consumers for idempotent
 * processing), the affected {@link #cartId()}, the {@link #occurredAt()} timestamp and a stable
 * {@link #eventType()} discriminator for dispatching/reporting.
 *
 * <p>The concrete subtype is recovered on the consumer side via Jackson deduction (each subtype has
 * a unique set of fields), so no explicit type header or discriminator field is required on the
 * wire.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(ItemAddedEvent.class),
    @JsonSubTypes.Type(ItemQuantityAdjustedEvent.class),
    @JsonSubTypes.Type(ItemRemovedEvent.class),
    @JsonSubTypes.Type(CartCheckedOutEvent.class)
})
public sealed interface CartEventMessage
        permits ItemAddedEvent, ItemQuantityAdjustedEvent, ItemRemovedEvent, CartCheckedOutEvent {

    UUID eventId();

    String cartId();

    Instant occurredAt();

    String eventType();
}
