package com.jeweleryshop.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jeweleryshop.backend.entity.Cart;
import com.jeweleryshop.backend.entity.User;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // ✅ Lấy giỏ hàng theo userId (đã có sẵn)
    Optional<Cart> findByUserId(Long userId);

    // ✅ Thêm mới: Lấy giỏ hàng theo entity User
    Optional<Cart> findByUser(User user);
}
