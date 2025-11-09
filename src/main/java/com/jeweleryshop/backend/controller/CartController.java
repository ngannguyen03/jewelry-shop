package com.jeweleryshop.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.jeweleryshop.backend.dto.AddItemToCartRequest;
import com.jeweleryshop.backend.dto.CartResponseDTO;
import com.jeweleryshop.backend.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ✅ Lấy giỏ hàng hiện tại của user đang đăng nhập
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart() {
        CartResponseDTO cart = cartService.getCart();
        return ResponseEntity.ok(cart);
    }

    // ✅ Lấy giỏ hàng theo userId (nếu cần dùng riêng)
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponseDTO> getCartByUser(@PathVariable Long userId) {
        CartResponseDTO cart = cartService.getOrCreateCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    // ✅ Thêm sản phẩm vào giỏ hàng
    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItemToCart(@Valid @RequestBody AddItemToCartRequest request) {
        CartResponseDTO updatedCart = cartService.addItemToCart(request);
        return ResponseEntity.ok(updatedCart);
    }

    // ✅ Xóa một sản phẩm khỏi giỏ hàng
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long cartItemId) {
        cartService.removeItemFromCart(cartItemId);
        return ResponseEntity.noContent().build();
    }

    // ✅ Cập nhật số lượng sản phẩm trong giỏ hàng
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {
        CartResponseDTO updatedCart = cartService.updateCartItemQuantity(cartItemId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    // ✅ Xóa toàn bộ giỏ hàng
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            cartService.clearCartByUser(email);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Không thể xóa toàn bộ giỏ hàng: " + e.getMessage());
        }
    }
}
