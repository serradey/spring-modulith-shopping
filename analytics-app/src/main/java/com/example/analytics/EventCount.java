package com.example.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Cumulative count of consumed events for a single event type. */
@Entity
@Table(name = "event_count")
class EventCount {

    @Id
    @Column(name = "event_type")
    private String eventType;

    @Column(name = "count", nullable = false)
    private long count;

    protected EventCount() {}

    EventCount(String eventType, long count) {
        this.eventType = eventType;
        this.count = count;
    }

    String eventType() {
        return eventType;
    }

    long count() {
        return count;
    }

    void increment() {
        this.count++;
    }
}
