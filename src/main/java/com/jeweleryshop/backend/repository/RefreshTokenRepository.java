package com.jeweleryshop.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.entity.RefreshToken;
import com.jeweleryshop.backend.entity.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 🔹 Tìm theo chuỗi token
    Optional<RefreshToken> findByToken(String token);

    // 🔹 Tìm token theo user
    Optional<RefreshToken> findByUser(User user);

    // 🔹 Xóa token cũ của user (an toàn, chắc chắn xóa khỏi DB)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM refresh_tokens WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Long userId);
}
