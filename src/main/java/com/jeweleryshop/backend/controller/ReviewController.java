package com.jeweleryshop.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeweleryshop.backend.dto.ReviewRequestDTO;
import com.jeweleryshop.backend.dto.ReviewResponseDTO;
import com.jeweleryshop.backend.service.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/variants/{variantId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getVariantReviews(@PathVariable Long variantId) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsForVariant(variantId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @PathVariable Long variantId,
            @Valid @RequestBody ReviewRequestDTO requestDTO) {
        ReviewResponseDTO createdReview = reviewService.createReview(variantId, requestDTO);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }
}
