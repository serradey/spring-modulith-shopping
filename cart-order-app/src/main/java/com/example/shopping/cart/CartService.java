package com.example.shopping.cart;

import com.example.shopping.cart.internal.ItemPopularityService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for the cart use cases. Each method runs in a single transaction; domain
 * events are published via {@link ApplicationEventPublisher} and dispatched (in-process and to
 * Kafka) after the transaction commits.
 */
@Service
public class CartService {

    private final CartRepository carts;
    private final ItemPopularityService itemPopularity;
    private final ApplicationEventPublisher events;

    CartService(
            CartRepository carts,
            ItemPopularityService itemPopularity,
            ApplicationEventPublisher events) {
        this.carts = carts;
        this.itemPopularity = itemPopularity;
        this.events = events;
    }

    @Transactional
    public CartSummary addItem(String cartId, String itemId, int quantity) {
        ShoppingCart cart =
                carts.findById(CartId.of(cartId)).orElseGet(() -> ShoppingCart.newCart(CartId.of(cartId)));
        ItemAdded event = cart.addItem(itemId, quantity);
        carts.save(cart);
        events.publishEvent(event);
        return summaryOf(cart);
    }

    @Transactional
    public CartSummary removeItem(String cartId, String itemId) {
        ShoppingCart cart = load(cartId);
        cart.removeItem(itemId).ifPresent(events::publishEvent);
        carts.save(cart);
        return summaryOf(cart);
    }

    @Transactional
    public CartSummary updateItem(String cartId, String itemId, int quantity) {
        if (quantity == 0) {
            return removeItem(cartId, itemId);
        }
        ShoppingCart cart = load(cartId);
        ItemQuantityAdjusted event = cart.adjustItem(itemId, quantity);
        carts.save(cart);
        events.publishEvent(event);
        return summaryOf(cart);
    }

    @Transactional
    public CartSummary checkout(String cartId) {
        ShoppingCart cart = load(cartId);
        CartCheckedOut event = cart.checkout();
        carts.save(cart);
        events.publishEvent(event);
        return summaryOf(cart);
    }

    @Transactional(readOnly = true)
    public CartSummary getCart(String cartId) {
        ShoppingCart cart = load(cartId);
        if (cart.isEmpty()) {
            throw new CartNotFoundException(cartId);
        }
        return summaryOf(cart);
    }

    @Transactional(readOnly = true)
    public long getItemPopularity(String itemId) {
        return itemPopularity.popularityOf(itemId);
    }

    private ShoppingCart load(String cartId) {
        return carts.findById(CartId.of(cartId)).orElseThrow(() -> new CartNotFoundException(cartId));
    }

    private static CartSummary summaryOf(ShoppingCart cart) {
        return new CartSummary(cart.itemsAsMap(), cart.isCheckedOut());
    }
}
