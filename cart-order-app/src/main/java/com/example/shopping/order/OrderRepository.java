package com.example.shopping.order;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<Order, OrderId> {

    boolean existsByCartId(String cartId);

    Optional<Order> findByCartId(String cartId);
}
