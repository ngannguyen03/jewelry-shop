package com.jeweleryshop.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeweleryshop.backend.dto.ProductResponseDTO;
import com.jeweleryshop.backend.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
@PreAuthorize("hasRole('USER')")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getWishlist() {
        List<ProductResponseDTO> wishlist = wishlistService.getWishlist();
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/{variantId}")
    public ResponseEntity<Void> addProductToWishlist(@PathVariable Long variantId) {
        wishlistService.addProductToWishlist(variantId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<Void> removeProductFromWishlist(@PathVariable Long variantId) {
        wishlistService.removeProductFromWishlist(variantId);
        return ResponseEntity.noContent().build();
    }
}
