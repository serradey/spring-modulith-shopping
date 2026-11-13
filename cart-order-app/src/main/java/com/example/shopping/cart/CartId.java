package com.example.shopping.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import org.jmolecules.ddd.types.Identifier;

/** Caller-supplied identifier of a {@link ShoppingCart}. */
@Embeddable
public class CartId implements Identifier, Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private String value;

    protected CartId() {}

    public CartId(String value) {
        this.value = Objects.requireNonNull(value, "cart id must not be null");
    }

    public static CartId of(String value) {
        return new CartId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CartId other)) {
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
        return value;
    }
}
