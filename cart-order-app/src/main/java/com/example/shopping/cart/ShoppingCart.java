package com.example.shopping.cart;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jmolecules.ddd.types.AggregateRoot;

/**
 * The cart aggregate and single source of truth for cart state. Enforces the rules described in
 * {@code docs/requirements/shopping-cart.feature}. Business methods mutate state and return the
 * resulting domain event; the application service is responsible for publishing it.
 */
@Entity
@Table(name = "cart")
public class ShoppingCart implements AggregateRoot<ShoppingCart, CartId> {

    @EmbeddedId
    private CartId id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_item", joinColumns = @JoinColumn(name = "cart_id"))
    private List<CartLine> items = new ArrayList<>();

    private Instant checkoutDate;

    protected ShoppingCart() {}

    private ShoppingCart(CartId id) {
        this.id = id;
    }

    public static ShoppingCart newCart(CartId id) {
        return new ShoppingCart(id);
    }

    @Override
    public CartId getId() {
        return id;
    }

    public boolean isCheckedOut() {
        return checkoutDate != null;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Map<String, Integer> itemsAsMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        items.forEach(line -> map.put(line.itemId(), line.quantity()));
        return map;
    }

    // --- behaviour -----------------------------------------------------------------------------

    public ItemAdded addItem(String itemId, int quantity) {
        if (isCheckedOut()) {
            throw new CartException("Can't add an item to an already checked out shopping cart");
        }
        if (hasItem(itemId)) {
            throw new CartException("Item '" + itemId + "' was already added to this shopping cart");
        }
        if (quantity <= 0) {
            throw new CartException("Quantity must be greater than zero");
        }
        items.add(new CartLine(itemId, quantity));
        return new ItemAdded(id.value(), itemId, quantity);
    }

    public ItemQuantityAdjusted adjustItem(String itemId, int quantity) {
        if (isCheckedOut()) {
            throw new CartException("Can't adjust item on an already checked out shopping cart");
        }
        if (quantity <= 0) {
            throw new CartException("Quantity must be greater than zero");
        }
        CartLine line = lineFor(itemId)
                .orElseThrow(() -> new CartException(
                        "Cannot adjust quantity for item '" + itemId + "'. Item not present on cart"));
        int oldQuantity = line.quantity();
        line.setQuantity(quantity);
        return new ItemQuantityAdjusted(id.value(), itemId, oldQuantity, quantity);
    }

    /**
     * Removes an item. Removing an item that is not present is a safe no-op (no event), so the
     * result is empty in that case.
     */
    public Optional<ItemRemoved> removeItem(String itemId) {
        if (isCheckedOut()) {
            throw new CartException("Can't remove an item from an already checked out shopping cart");
        }
        Optional<CartLine> line = lineFor(itemId);
        if (line.isEmpty()) {
            return Optional.empty();
        }
        int removed = line.get().quantity();
        items.remove(line.get());
        return Optional.of(new ItemRemoved(id.value(), itemId, removed));
    }

    public CartCheckedOut checkout() {
        if (isCheckedOut()) {
            throw new CartException("Can't checkout already checked out shopping cart");
        }
        if (isEmpty()) {
            throw new CartException("Cannot checkout an empty shopping cart");
        }
        this.checkoutDate = Instant.now();
        return new CartCheckedOut(id.value(), itemsAsMap(), checkoutDate);
    }

    // --- helpers -------------------------------------------------------------------------------

    private boolean hasItem(String itemId) {
        return lineFor(itemId).isPresent();
    }

    private Optional<CartLine> lineFor(String itemId) {
        return items.stream().filter(line -> line.itemId().equals(itemId)).findFirst();
    }
}
