package com.jeweleryshop.backend.service;

import com.jeweleryshop.backend.entity.User;
import com.jeweleryshop.backend.entity.Wishlist;
import com.jeweleryshop.backend.entity.ProductVariant;
import com.jeweleryshop.backend.exception.AppException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.ProductVariantRepository;
import com.jeweleryshop.backend.repository.UserRepository;
import com.jeweleryshop.backend.repository.WishlistRepository;
import com.jeweleryshop.backend.dto.ProductResponseDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;
    private final WishlistRepository wishlistRepository;
    private final ProductService productService; // Reuse the conversion logic

    public WishlistService(UserRepository userRepository, ProductVariantRepository variantRepository, WishlistRepository wishlistRepository, ProductService productService) {
        this.userRepository = userRepository;
        this.variantRepository = variantRepository;
        this.wishlistRepository = wishlistRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getWishlist() {
        User currentUser = getCurrentUser();
        List<Wishlist> wishlistItems = wishlistRepository.findByUserId(currentUser.getId());
        return wishlistItems.stream()
                .map(item -> productService.convertToResponseDTO(item.getVariant().getProduct()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addProductToWishlist(Long variantId) {
        User currentUser = getCurrentUser();
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + variantId));

        if (wishlistRepository.existsByUserIdAndVariantId(currentUser.getId(), variantId)) {
            throw new AppException("Product is already in the wishlist.");
        }

        Wishlist wishlistItem = new Wishlist();
        wishlistItem.setUser(currentUser);
        wishlistItem.setVariant(variant);
        wishlistRepository.save(wishlistItem);
    }

    @Transactional
    public void removeProductFromWishlist(Long variantId) {
        User currentUser = getCurrentUser();
        Wishlist wishlistItem = wishlistRepository.findByUserIdAndVariantId(currentUser.getId(), variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in wishlist."));

        wishlistRepository.delete(wishlistItem);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }
}
