package com.example.shopping.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** A line of an {@link Order}: an item and the ordered quantity. */
@Embeddable
public class OrderLine {

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    protected OrderLine() {}

    OrderLine(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public String itemId() {
        return itemId;
    }

    public int quantity() {
        return quantity;
    }
}
