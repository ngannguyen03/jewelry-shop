package com.jeweleryshop.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jeweleryshop.backend.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByVariantId(Long variantId);

    Optional<Review> findByVariantIdAndUserId(Long variantId, Long userId);
}
