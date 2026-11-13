package com.example.shopping.cart;

import org.springframework.data.jpa.repository.JpaRepository;

interface CartRepository extends JpaRepository<ShoppingCart, CartId> {}
