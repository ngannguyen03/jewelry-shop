package com.jeweleryshop.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jeweleryshop.backend.entity.Cart;
import com.jeweleryshop.backend.entity.CartItem;
import com.jeweleryshop.backend.entity.ProductVariant;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);
}
