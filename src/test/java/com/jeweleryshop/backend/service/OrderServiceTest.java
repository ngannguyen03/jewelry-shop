package com.jeweleryshop.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.jeweleryshop.backend.dto.CreateOrderRequestDTO;
import com.jeweleryshop.backend.dto.OrderDetailResponseDTO;
import com.jeweleryshop.backend.entity.Address;
import com.jeweleryshop.backend.entity.Cart;
import com.jeweleryshop.backend.entity.CartItem;
import com.jeweleryshop.backend.entity.Inventory;
import com.jeweleryshop.backend.entity.Order;
import com.jeweleryshop.backend.entity.OrderDetail;
import com.jeweleryshop.backend.entity.OrderStatus;
import com.jeweleryshop.backend.entity.Product;
import com.jeweleryshop.backend.entity.ProductVariant;
import com.jeweleryshop.backend.entity.Promotion;
import com.jeweleryshop.backend.entity.User;
import com.jeweleryshop.backend.exception.AppException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.AddressRepository;
import com.jeweleryshop.backend.repository.CartRepository;
import com.jeweleryshop.backend.repository.OrderRepository;
import com.jeweleryshop.backend.repository.ProductVariantRepository;
import com.jeweleryshop.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private ProductVariantRepository variantRepository;
    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private CartItem cartItem;
    private ProductVariant variant;
    private Product product;
    private Inventory inventory;
    private Address address;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(ctx);

        address = new Address();
        address.setId(10L);
        address.setUser(user);
        address.setFullName("Nguyen Duy Anh Tuan");

        product = new Product();
        product.setId(100L);
        product.setName("Diamond Ring");
        product.setBasePrice(BigDecimal.valueOf(1000));
        product.setDiscountPrice(BigDecimal.valueOf(800));

        variant = new ProductVariant();
        variant.setId(200L);
        variant.setProduct(product);
        variant.setPriceModifier(BigDecimal.valueOf(50));

        inventory = new Inventory();
        inventory.setQuantity(10);
        variant.setInventory(inventory);

        cartItem = new CartItem();
        cartItem.setId(300L);
        cartItem.setVariant(variant);
        cartItem.setQuantity(2);

        cart = new Cart();
        cart.setId(400L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    }

    // ✅ 1. Tạo đơn hàng thành công
    @Test
    void testCreateOrder_Success() {
        CreateOrderRequestDTO req = new CreateOrderRequestDTO();
        req.setShippingAddressId(10L);
        req.setDiscountCode("SALE10");

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(variantRepository.findWithLockingById(anyLong())).thenReturn(Optional.of(variant));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderDetailResponseDTO dto = orderService.createOrder(req);
        assertNotNull(dto);
        assertTrue(dto.getFinalTotal().compareTo(BigDecimal.ZERO) > 0);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // ✅ 2. Giỏ hàng trống
    @Test
    void testCreateOrder_EmptyCart_ShouldThrow() {
        cart.getItems().clear();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        CreateOrderRequestDTO req = new CreateOrderRequestDTO();
        assertThrows(AppException.class, () -> orderService.createOrder(req));
    }

    // ✅ 3. Địa chỉ không tồn tại
    @Test
    void testCreateOrder_AddressNotFound_ShouldThrow() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(10L)).thenReturn(Optional.empty());
        CreateOrderRequestDTO req = new CreateOrderRequestDTO();
        req.setShippingAddressId(10L);
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(req));
    }

// ✅ 4. Địa chỉ không thuộc user
    @Test
    void testCreateOrder_AddressNotBelongToUser_ShouldThrow() {
        User anotherUser = new User();
        anotherUser.setId(999L);

        Address other = new Address();
        other.setId(99L);
        other.setUser(anotherUser);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(99L)).thenReturn(Optional.of(other));

        CreateOrderRequestDTO req = new CreateOrderRequestDTO();
        req.setShippingAddressId(99L);

        assertThrows(AppException.class, () -> orderService.createOrder(req));
    }

    // ✅ 5. ProductVariant không tồn tại
    @Test
    void testCreateOrder_VariantNotFound_ShouldThrow() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(variantRepository.findWithLockingById(anyLong())).thenReturn(Optional.empty());
        CreateOrderRequestDTO req = new CreateOrderRequestDTO();
        req.setShippingAddressId(10L);
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(req));
    }

    // ✅ 6. Tồn kho không đủ
    @Test
    void testCreateOrder_NotEnoughStock_ShouldThrow() {
        inventory.setQuantity(1);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(variantRepository.findWithLockingById(anyLong())).thenReturn(Optional.of(variant));
        CreateOrderRequestDTO req = new CreateOrderRequestDTO();
        req.setShippingAddressId(10L);
        assertThrows(AppException.class, () -> orderService.createOrder(req));
    }

    // ✅ 7. Tạo đơn hàng có promotion trong cart
    @Test
    void testCreateOrder_WithPromotionInCart_ShouldCallIncrementUsage() {
        Promotion promo = new Promotion();
        promo.setCode("SALE10");
        cart.setAppliedPromotion(promo);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(variantRepository.findWithLockingById(anyLong())).thenReturn(Optional.of(variant));
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());

        CreateOrderRequestDTO req = new CreateOrderRequestDTO();
        req.setShippingAddressId(10L);
        orderService.createOrder(req);

        verify(promotionService, times(1)).incrementUsage("SALE10");
    }

    // ✅ 8. Lấy danh sách đơn hàng cho current user
    @Test
    void testGetOrdersForCurrentUser_ShouldReturnList() {
        Order o = new Order();
        o.setId(1L);
        o.setUser(user);
        o.setOrderDetails(Set.of());
        when(orderRepository.findByUserIdOrderByOrderDateDesc(1L)).thenReturn(List.of(o));

        List<OrderDetailResponseDTO> result = orderService.getOrdersForCurrentUser();
        assertEquals(1, result.size());
    }

    // ✅ 9. Lấy chi tiết đơn hàng (hợp lệ)
    @Test
    void testGetOrderDetails_Success() {
        Order o = new Order();
        o.setId(10L);
        o.setUser(user);
        o.setOrderDetails(Set.of());
        when(orderRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(o));

        OrderDetailResponseDTO dto = orderService.getOrderDetails(10L);
        assertNotNull(dto);
    }

// ✅ 10. Lấy chi tiết đơn hàng không thuộc user
    @Test
    void testGetOrderDetails_NotOwned_ShouldThrow() {
        User anotherUser = new User();
        anotherUser.setId(999L);

        Order o = new Order();
        o.setUser(anotherUser);
        o.setOrderDetails(Set.of());

        when(orderRepository.findByIdWithDetails(99L)).thenReturn(Optional.of(o));

        assertThrows(AppException.class, () -> orderService.getOrderDetails(99L));
    }

    // ✅ 11. Lấy chi tiết đơn hàng không tồn tại
    @Test
    void testGetOrderDetails_NotFound_ShouldThrow() {
        when(orderRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderDetails(999L));
    }

    // ✅ 12. Lấy tất cả đơn hàng (Admin)
    @Test
    void testGetAllOrders_ShouldReturnPage() {
        Order o = new Order();
        o.setId(1L);
        o.setUser(user);
        o.setOrderDetails(Set.of());
        Page<Order> page = new PageImpl<>(List.of(o));
        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<OrderDetailResponseDTO> result = orderService.getAllOrders(PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
    }

    // ✅ 13. Cập nhật trạng thái đơn hàng → CANCELLED hoàn kho
    @Test
    void testUpdateOrderStatus_Cancelled_ShouldRestock() {
        OrderDetail detail = new OrderDetail();
        detail.setQuantity(2);
        detail.setVariant(variant);

        Order o = new Order();
        o.setId(1L);
        o.setStatus(OrderStatus.PENDING);
        o.setOrderDetails(Set.of(detail));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));
        when(orderRepository.save(any(Order.class))).thenReturn(o);

        OrderDetailResponseDTO dto = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, dto.getStatus());
    }

    // ✅ 14. Cập nhật trạng thái đơn hàng không tồn tại
    @Test
    void testUpdateOrderStatus_NotFound_ShouldThrow() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrderStatus(999L, OrderStatus.SHIPPED));
    }
}
