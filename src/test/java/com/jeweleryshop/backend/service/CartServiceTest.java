package com.jeweleryshop.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.jeweleryshop.backend.dto.AddItemToCartRequest;
import com.jeweleryshop.backend.dto.CartResponseDTO;
import com.jeweleryshop.backend.entity.Cart;
import com.jeweleryshop.backend.entity.CartItem;
import com.jeweleryshop.backend.entity.Inventory;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductVariantRepository variantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private ProductVariant variant;
    private CartItem item;
    private Cart cart;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        product = new Product();
        product.setId(1L);
        product.setName("Gold Ring");
        product.setBasePrice(BigDecimal.valueOf(1000));
        product.setDiscountPrice(BigDecimal.valueOf(800));

        ProductImage image = new ProductImage();
        image.setId(1L);
        image.setImageUrl("img.jpg");
        image.setProduct(product);
        Set<ProductImage> images = new HashSet<>();
        images.add(image);
        product.setImages(images);

        variant = new ProductVariant();
        variant.setId(1L);
        variant.setProduct(product);
        variant.setPriceModifier(BigDecimal.valueOf(50));

        Inventory inventory = new Inventory();
        inventory.setQuantity(10);
        variant.setInventory(inventory);

        item = new CartItem();
        item.setId(10L);
        item.setVariant(variant);
        item.setQuantity(2);

        cart = new Cart();
        cart.setId(5L);
        cart.setUser(user);
        List<CartItem> items = new ArrayList<>();
        items.add(item);
        cart.setItems(items);

        // Mock user context
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(ctx);

        // Default mock user
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
    }

    // ✅ getOrCreateCartByUserId (có giỏ)
    @Test
    void testGetOrCreateCartByUserId_ShouldReturnExisting() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        CartResponseDTO dto = cartService.getOrCreateCartByUserId(1L);
        assert dto.getItems().size() == 1;
    }

    // ✅ getOrCreateCartByUserId (tạo mới)
    @Test
    void testGetOrCreateCartByUserId_CreateNewIfNotExists() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponseDTO dto = cartService.getOrCreateCartByUserId(1L);
        assert dto != null;
    }

    // ✅ createCartIfNotExists (tạo mới)
    @Test
    void testCreateCartIfNotExists_WhenNotExists_ShouldCreate() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.createCartIfNotExists(1L);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // ✅ createCartIfNotExists (user không tồn tại)
    @Test
    void testCreateCartIfNotExists_UserNotFound() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        try {
            cartService.createCartIfNotExists(1L);
            assert false;
        } catch (ResourceNotFoundException e) {
            assert e.getMessage().contains("Không tìm thấy user ID");
        }
    }

    // ✅ addItemToCart (thêm mới)
    @Test
    void testAddItemToCart_NewItemSuccess() {
        AddItemToCartRequest req = new AddItemToCartRequest();
        req.setProductVariantId(1L);
        req.setQuantity(1);

        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(cartItemRepository.findByCartAndVariant(cart, variant)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO dto = cartService.addItemToCart(req);
        assert dto != null;
    }

    // ✅ addItemToCart (tồn tại, cập nhật số lượng)
    @Test
    void testAddItemToCart_ExistingItemIncreasesQuantity() {
        AddItemToCartRequest req = new AddItemToCartRequest();
        req.setProductVariantId(1L);
        req.setQuantity(2);

        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(cartItemRepository.findByCartAndVariant(cart, variant)).thenReturn(Optional.of(item));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO dto = cartService.addItemToCart(req);
        assert dto != null;
    }

    // ✅ addItemToCart (hết hàng)
    @Test
    void testAddItemToCart_ThrowsAppException_WhenOutOfStock() {
        variant.getInventory().setQuantity(0);

        AddItemToCartRequest req = new AddItemToCartRequest();
        req.setProductVariantId(1L);
        req.setQuantity(5);

        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(cartItemRepository.findByCartAndVariant(cart, variant)).thenReturn(Optional.empty());

        try {
            cartService.addItemToCart(req);
            assert false;
        } catch (AppException e) {
            assert e.getMessage().contains("Không đủ hàng");
        }
    }

    // ✅ removeItemFromCart
    @Test
    void testRemoveItemFromCart_Success() {
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        cartService.removeItemFromCart(10L);
        verify(cartRepository, times(1)).save(cart);
    }

    // ✅ removeItemFromCart (item không tồn tại)
    @Test
    void testRemoveItemFromCart_ItemNotFound() {
        cart.getItems().clear();
        try {
            cartService.removeItemFromCart(999L);
            assert false;
        } catch (ResourceNotFoundException e) {
            assert e.getMessage().contains("Không tìm thấy sản phẩm");
        }
    }

    // ✅ clearCartByUser (thành công)
    @Test
    void testClearCartByUser_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.clearCartByUser("test@example.com");
        verify(cartRepository, times(1)).save(cart);
    }

    // ✅ clearCartByUser (user không tồn tại)
    @Test
    void testClearCartByUser_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        try {
            cartService.clearCartByUser("notfound@example.com");
            assert false;
        } catch (ResourceNotFoundException e) {
            assert e.getMessage().contains("Không tìm thấy người dùng");
        }
    }

    // ✅ clearCartByUser (cart không tồn tại)
    @Test
    void testClearCartByUser_CartNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
        try {
            cartService.clearCartByUser("test@example.com");
            assert false;
        } catch (ResourceNotFoundException e) {
            assert e.getMessage().contains("Không tìm thấy giỏ hàng");
        }
    }

    // ✅ updateCartItemQuantity (giảm về 0 → xóa)
    @Test
    void testUpdateCartItemQuantity_RemoveItemWhenZero() {
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        CartResponseDTO dto = cartService.updateCartItemQuantity(10L, 0);
        assert dto != null;
    }

    // ✅ updateCartItemQuantity (vượt tồn kho)
    @Test
    void testUpdateCartItemQuantity_ThrowsAppException_WhenOutOfStock() {
        variant.getInventory().setQuantity(1);
        try {
            cartService.updateCartItemQuantity(10L, 5);
            assert false;
        } catch (AppException e) {
            assert e.getMessage().contains("Không đủ hàng");
        }
    }

    // ✅ getCart (đầy đủ)
    @Test
    void testGetCart_ShouldReturnCart() {
        CartResponseDTO dto = cartService.getCart();
        assert dto != null;
        assert dto.getTotalItems() == 1;
    }
}
