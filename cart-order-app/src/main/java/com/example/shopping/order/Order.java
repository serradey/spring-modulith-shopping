package com.example.shopping.order;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.jmolecules.ddd.types.AggregateRoot;

/** An order created from a checked-out cart. */
@Entity
@Table(name = "orders")
public class Order implements AggregateRoot<Order, OrderId> {

    @EmbeddedId
    private OrderId id;

    @Column(name = "cart_id", nullable = false, unique = true)
    private String cartId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_line", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderLine> lines;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Order() {}

    private Order(OrderId id, String cartId, List<OrderLine> lines, Instant createdAt) {
        this.id = id;
        this.cartId = cartId;
        this.lines = lines;
        this.createdAt = createdAt;
    }

    public static Order fromCart(String cartId, Map<String, Integer> items) {
        List<OrderLine> lines = items.entrySet().stream()
                .map(entry -> new OrderLine(entry.getKey(), entry.getValue()))
                .toList();
        return new Order(OrderId.newId(), cartId, lines, Instant.now());
    }

    @Override
    public OrderId getId() {
        return id;
    }

    public String cartId() {
        return cartId;
    }

    public List<OrderLine> lines() {
        return List.copyOf(lines);
    }

    public Instant createdAt() {
        return createdAt;
    }

    /** Total number of units across all lines. */
    public int totalUnits() {
        return lines.stream().mapToInt(OrderLine::quantity).sum();
    }
}
