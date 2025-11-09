package com.jeweleryshop.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.dto.AddItemToCartRequest;
import com.jeweleryshop.backend.dto.CartResponseDTO;
import com.jeweleryshop.backend.entity.Cart;
import com.jeweleryshop.backend.entity.CartItem;
import com.jeweleryshop.backend.entity.Product;
import com.jeweleryshop.backend.entity.ProductImage;
import com.jeweleryshop.backend.entity.ProductVariant;
import com.jeweleryshop.backend.entity.User;
import com.jeweleryshop.backend.exception.AppException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.CartItemRepository;
import com.jeweleryshop.backend.repository.CartRepository;
import com.jeweleryshop.backend.repository.ProductVariantRepository;
import com.jeweleryshop.backend.repository.UserRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final PromotionService promotionService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
            ProductVariantRepository variantRepository, UserRepository userRepository,
            PromotionService promotionService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.promotionService = promotionService;
    }

    // ✅ Tạo giỏ nếu chưa có
    public void createCartIfNotExists(Long userId) {
        cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user ID: " + userId));
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    // ✅ Lấy giỏ hàng theo userId
    @Transactional(readOnly = true)
    public CartResponseDTO getOrCreateCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user ID: " + userId));
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
        return convertToDTO(cart);
    }

    // ✅ Thêm sản phẩm vào giỏ hàng
    @Transactional
    public CartResponseDTO addItemToCart(AddItemToCartRequest request) {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        ProductVariant variant = variantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy Product Variant ID: " + request.getProductVariantId()));

        int currentStock = (variant.getInventory() != null)
                ? variant.getInventory().getQuantity()
                : 0;

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndVariant(cart, variant);

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            if (currentStock < newQuantity) {
                throw new AppException("Không đủ hàng trong kho cho sản phẩm: " + variant.getProduct().getName());
            }

            existingItem.setQuantity(newQuantity);
        } else {
            if (currentStock < request.getQuantity()) {
                throw new AppException("Không đủ hàng trong kho cho sản phẩm: " + variant.getProduct().getName());
            }

            CartItem newItem = new CartItem();
            newItem.setVariant(variant);
            newItem.setQuantity(request.getQuantity());
            cart.addItem(newItem);
        }

        cartRepository.save(cart);
        return convertToDTO(cart);
    }

    // ✅ Lấy giỏ hàng hiện tại
    @Transactional(readOnly = true)
    public CartResponseDTO getCart() {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        return convertToDTO(cart);
    }

    // ✅ Xóa item khỏi giỏ hàng
    @Transactional
    public void removeItemFromCart(Long cartItemId) {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));
        cart.removeItem(itemToRemove);
        cartRepository.save(cart);
    }

    // ✅ Xóa toàn bộ giỏ hàng
    @Transactional
    public void clearCartByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + email));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng của user: " + email));

        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }

    // ✅ NEW: Cập nhật số lượng sản phẩm trong giỏ hàng
    @Transactional
    public CartResponseDTO updateCartItemQuantity(Long cartItemId, int quantity) {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // Nếu quantity <= 0 → tự động xóa item
        if (quantity <= 0) {
            cart.removeItem(item);
            cartRepository.save(cart);
            return convertToDTO(cart);
        }

        // Kiểm tra tồn kho
        ProductVariant variant = item.getVariant();
        int stock = (variant.getInventory() != null) ? variant.getInventory().getQuantity() : 0;
        if (quantity > stock) {
            throw new AppException("Không đủ hàng trong kho cho sản phẩm: " + variant.getProduct().getName());
        }

        item.setQuantity(quantity);
        cartRepository.save(cart);

        return convertToDTO(cart);
    }

    // =================== TIỆN ÍCH ===================
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    private CartResponseDTO convertToDTO(Cart cart) {
        List<CartResponseDTO.CartItemResponseDTO> items = cart.getItems().stream()
                .map(item -> {
                    ProductVariant variant = item.getVariant();
                    Product product = variant.getProduct();
                    BigDecimal base = (product.getDiscountPrice() != null && product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
                            ? product.getDiscountPrice()
                            : product.getBasePrice();

                    BigDecimal price = base.add(variant.getPriceModifier() != null ? variant.getPriceModifier() : BigDecimal.ZERO);

                    String imageUrl = product.getImages().stream()
                            .findFirst().map(ProductImage::getImageUrl).orElse(null);

                    return new CartResponseDTO.CartItemResponseDTO(
                            item.getId(),
                            variant.getId(),
                            product.getName(),
                            price,
                            item.getQuantity(),
                            imageUrl
                    );
                })
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(cart.getId(), items, items.size(),
                total, BigDecimal.ZERO, total, null);
    }
}
