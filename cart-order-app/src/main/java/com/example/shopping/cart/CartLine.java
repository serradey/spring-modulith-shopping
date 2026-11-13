package com.example.shopping.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** A line item held by a {@link ShoppingCart}: an item and its (positive) quantity. */
@Embeddable
public class CartLine {

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    protected CartLine() {}

    CartLine(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public String itemId() {
        return itemId;
    }

    public int quantity() {
        return quantity;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
