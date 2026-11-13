package com.example.shopping.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CartController {

    private final CartService cartService;

    CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/carts/{cartId}/items")
    CartSummary addItem(@PathVariable String cartId, @Valid @RequestBody AddItemRequest request) {
        return cartService.addItem(cartId, request.itemId(), request.quantity());
    }

    @PutMapping("/carts/{cartId}/items/{itemId}")
    CartSummary updateItem(
            @PathVariable String cartId,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateItemRequest request) {
        return cartService.updateItem(cartId, itemId, request.quantity());
    }

    @DeleteMapping("/carts/{cartId}/items/{itemId}")
    CartSummary removeItem(@PathVariable String cartId, @PathVariable String itemId) {
        return cartService.removeItem(cartId, itemId);
    }

    @PostMapping("/carts/{cartId}/checkout")
    CartSummary checkout(@PathVariable String cartId) {
        return cartService.checkout(cartId);
    }

    @GetMapping("/carts/{cartId}")
    CartSummary getCart(@PathVariable String cartId) {
        return cartService.getCart(cartId);
    }

    @GetMapping("/items/{itemId}/popularity")
    PopularityResponse popularity(@PathVariable String itemId) {
        return new PopularityResponse(itemId, cartService.getItemPopularity(itemId));
    }

    record AddItemRequest(@NotBlank String itemId, int quantity) {}

    record UpdateItemRequest(int quantity) {}

    record PopularityResponse(String itemId, long popularityCount) {}
}
