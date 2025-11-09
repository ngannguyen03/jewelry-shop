package com.jeweleryshop.backend.service;

import com.jeweleryshop.backend.dto.ReviewRequestDTO;
import com.jeweleryshop.backend.dto.ReviewResponseDTO;
import com.jeweleryshop.backend.entity.*;
import com.jeweleryshop.backend.exception.AppException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.OrderRepository;
import com.jeweleryshop.backend.repository.ProductVariantRepository;
import com.jeweleryshop.backend.repository.ProductRepository;
import com.jeweleryshop.backend.repository.ReviewRepository;
import com.jeweleryshop.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductVariantRepository variantRepository, UserRepository userRepository, OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ReviewResponseDTO createReview(Long variantId, ReviewRequestDTO requestDTO) {
        User currentUser = getCurrentUser();
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found with id: " + variantId));

        // Business Rule: Only users who have purchased the product can review it.
        List<Order> deliveredOrders = orderRepository.findByUserIdAndStatus(currentUser.getId(), OrderStatus.DELIVERED);

        boolean hasPurchased = deliveredOrders.stream()
                .anyMatch(order -> order.getOrderDetails().stream()
                .anyMatch(detail -> detail.getVariant().getId().equals(variantId)));

        if (!hasPurchased) {
            throw new AppException("You can only review products you have purchased and received.");
        }

        // Find the specific order detail to link the review to an order
        Order orderForReview = deliveredOrders.stream()
                .filter(order -> order.getOrderDetails().stream().anyMatch(detail -> detail.getVariant().getId().equals(variantId)))
                .findFirst()
                .orElseThrow(() -> new AppException("Could not find a corresponding delivered order for this review."));

        // Business Rule: A user can only review a product once.
        reviewRepository.findByVariantIdAndUserId(variantId, currentUser.getId()).ifPresent(r -> {
            throw new AppException("You have already reviewed this product.");
        });

        Review review = new Review();
        review.setVariant(variant);
        review.setUser(currentUser);
        review.setOrder(orderForReview);
        review.setRating(requestDTO.getRating());
        review.setComment(requestDTO.getComment());
        review.setApproved(true); // Or set to false for admin approval

        Review savedReview = reviewRepository.save(review);

        return convertToDTO(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsForVariant(Long variantId) {
        if (!variantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("Product Variant not found with id: " + variantId);
        }
        return reviewRepository.findByVariantId(variantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private ReviewResponseDTO convertToDTO(Review review) {
        return new ReviewResponseDTO(review.getId(), review.getRating(), review.getComment(),
                review.getUser().getUsername(), review.getUser().getId(), review.getReviewDate());
    }
}
