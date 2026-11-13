package com.example.shopping.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

/** Identifier of an {@link Order}. */
@Embeddable
public class OrderId implements Identifier, Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private UUID value;

    protected OrderId() {}

    private OrderId(UUID value) {
        this.value = value;
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderId other)) {
            return false;
        }
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
