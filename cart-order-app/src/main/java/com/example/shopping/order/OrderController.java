package com.example.shopping.order;

import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class OrderController {

    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders/{cartId}")
    ResponseEntity<OrderResponse> byCart(@PathVariable String cartId) {
        return orderService
                .findByCartId(cartId)
                .map(OrderController::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static OrderResponse toResponse(Order order) {
        List<LineResponse> lines = order.lines().stream()
                .map(l -> new LineResponse(l.itemId(), l.quantity()))
                .toList();
        return new OrderResponse(
                order.getId().value().toString(),
                order.cartId(),
                lines,
                order.totalUnits(),
                order.createdAt());
    }

    record OrderResponse(
            String orderId, String cartId, List<LineResponse> items, int totalUnits, Instant createdAt) {}

    record LineResponse(String itemId, int quantity) {}
}
