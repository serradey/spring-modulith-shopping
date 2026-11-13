package com.example.shopping.cart.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read model holding the running, cross-cart popularity count for a single item. Maintained
 * asynchronously from cart events (see {@code docs/requirements/item-popularity.feature}).
 */
@Entity
@Table(name = "item_popularity")
public class ItemPopularity {

    @Id
    @Column(name = "item_id")
    private String itemId;

    @Column(name = "count", nullable = false)
    private long count;

    protected ItemPopularity() {}

    ItemPopularity(String itemId, long count) {
        this.itemId = itemId;
        this.count = count;
    }

    String itemId() {
        return itemId;
    }

    long count() {
        return count;
    }

    void changeBy(long delta) {
        this.count += delta;
    }
}
