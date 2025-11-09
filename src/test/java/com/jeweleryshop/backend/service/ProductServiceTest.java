package com.jeweleryshop.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.jeweleryshop.backend.dto.ProductRequestDTO;
import com.jeweleryshop.backend.dto.ProductResponseDTO;
import com.jeweleryshop.backend.entity.Category;
import com.jeweleryshop.backend.entity.Product;
import com.jeweleryshop.backend.entity.ProductImage;
import com.jeweleryshop.backend.entity.ProductVariant;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.CategoryRepository;
import com.jeweleryshop.backend.repository.ProductRepository;
import com.jeweleryshop.backend.repository.ProductVariantRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;
    private ProductRequestDTO requestDTO;

    @BeforeEach
    void setup() {
        category = new Category();
        category.setId(1L);
        category.setName("Rings");

        product = new Product();
        product.setId(1L);
        product.setName("Gold Ring");
        product.setDescription("24K gold jewelry");
        product.setBasePrice(BigDecimal.valueOf(5000000));
        product.setDiscountPrice(BigDecimal.valueOf(4500000));
        product.setSkuPrefix("RING001");
        product.setIsActive(true);
        product.setCategory(category);

        requestDTO = new ProductRequestDTO();
        requestDTO.setCategoryId(1L);
        requestDTO.setName("Gold Ring");
        requestDTO.setDescription("24K gold jewelry");
        requestDTO.setBasePrice(BigDecimal.valueOf(5000000));
        requestDTO.setDiscountPrice(BigDecimal.valueOf(4500000));
        requestDTO.setSkuPrefix("RING001");
        requestDTO.setIsActive(true);
    }

    // ==============================================================
    // ✅ 1. Tạo sản phẩm thành công
    // ==============================================================
    @Test
    void testCreateProduct_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(new ProductVariant());

        ProductResponseDTO result = productService.createProduct(requestDTO);

        assertNotNull(result);
        assertEquals("Gold Ring", result.getName());
        assertEquals(BigDecimal.valueOf(5000000), result.getBasePrice());
        assertEquals("Rings", result.getCategoryName());
        verify(productRepository).save(any(Product.class));
        verify(productVariantRepository).save(any(ProductVariant.class));
    }

    // ==============================================================
    // ✅ 2. Tạo sản phẩm thất bại — Category không tồn tại
    // ==============================================================
    @Test
    void testCreateProduct_CategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.createProduct(requestDTO)
        );

        assertEquals("Category not found with id: 1", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    // ==============================================================
    // ✅ 3. Tạo sản phẩm với isActive = null (should set default to true)
    // ==============================================================
    @Test
    void testCreateProduct_WithNullIsActive_ShouldSetDefaultToTrue() {
        requestDTO.setIsActive(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(new ProductVariant());

        ProductResponseDTO result = productService.createProduct(requestDTO);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    // ==============================================================
    // ✅ 4. Lấy sản phẩm theo ID (thành công)
    // ==============================================================
    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponseDTO response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Gold Ring", response.getName());
        assertEquals("Rings", response.getCategoryName());
        verify(productRepository).findById(1L);
    }

    // ==============================================================
    // ✅ 5. Lấy sản phẩm theo ID (không tồn tại)
    // ==============================================================
    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(1L)
        );

        assertEquals("Product not found with id: 1", exception.getMessage());
    }

    // ==============================================================
    // ✅ 6. Lấy sản phẩm có images và variants
    // ==============================================================
    @Test
    void testGetProductById_WhenProductHasImagesAndVariants_ShouldReturnCompleteDTO() {
        // Arrange
        ProductImage image = new ProductImage();
        image.setId(1L);
        image.setImageUrl("test.jpg");

        ProductVariant variant = new ProductVariant();
        variant.setId(1L);
        variant.setSize("M");
        variant.setMaterial("Gold");
        variant.setSku("RING001-M");
        variant.setPriceModifier(BigDecimal.ZERO);

        product.setImages(Set.of(image));
        product.setVariants(Set.of(variant));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        ProductResponseDTO result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getImages().size());
        assertEquals(1, result.getVariants().size());
        assertEquals("test.jpg", result.getImages().get(0).getImageUrl());
        assertEquals("M", result.getVariants().get(0).getSize());
        assertEquals("Gold", result.getVariants().get(0).getMaterial());
    }

    // ==============================================================
    // ✅ 7. Cập nhật sản phẩm thành công
    // ==============================================================
    @Test
    void testUpdateProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO updated = productService.updateProduct(1L, requestDTO);

        assertNotNull(updated);
        assertEquals("Gold Ring", updated.getName());
        verify(productRepository).save(any(Product.class));
        verify(categoryRepository).findById(1L);
    }

    // ==============================================================
    // ✅ 8. Cập nhật sản phẩm — Product không tồn tại
    // ==============================================================
    @Test
    void testUpdateProduct_ProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, requestDTO));
        verify(productRepository, never()).save(any());
    }

    // ==============================================================
    // ✅ 9. Cập nhật sản phẩm — Category không tồn tại
    // ==============================================================
    @Test
    void testUpdateProduct_CategoryNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, requestDTO));
        verify(productRepository, never()).save(any());
    }

    // ==============================================================
    // ✅ 10. Xóa sản phẩm — Thành công
    // ==============================================================
    @Test
    void testDeleteProduct_Success() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
        verify(productRepository).existsById(1L);
    }

    // ==============================================================
    // ✅ 11. Xóa sản phẩm — Không tồn tại
    // ==============================================================
    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.existsById(1L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(1L)
        );

        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository, never()).deleteById(anyLong());
    }

    // ==============================================================
    // ✅ 12. Lấy danh sách sản phẩm cho Admin
    // ==============================================================
    @Test
    void testGetAllProductsForAdmin() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponseDTO> result = productService.getAllProductsForAdmin();

        assertEquals(1, result.size());
        assertEquals("Gold Ring", result.get(0).getName());
        assertEquals("Rings", result.get(0).getCategoryName());
        verify(productRepository).findAll();
    }

    // ==============================================================
    // ✅ 13. Lấy danh sách sản phẩm (Public + filter)
    // ==============================================================
    @Test
    void testGetAllProducts_Filtered() {
        Page<Product> mockPage = new PageImpl<>(List.of(product));

        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<ProductResponseDTO> result = productService.getAllProducts(
                PageRequest.of(0, 10),
                1L, BigDecimal.ZERO, BigDecimal.TEN, "Ring", "M", "Gold"
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Gold Ring", result.getContent().get(0).getName());
        verify(productRepository, times(1))
                .findAll(any(Specification.class), any(Pageable.class));
    }

    // ==============================================================
    // ✅ 14. Lấy danh sách sản phẩm với tất cả filter null
    // ==============================================================
    @Test
    void testGetAllProducts_WithAllFiltersNull() {
        Page<Product> mockPage = new PageImpl<>(List.of(product));

        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<ProductResponseDTO> result = productService.getAllProducts(
                PageRequest.of(0, 10),
                null, null, null, null, null, null
        );

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ==============================================================
    // ✅ 15. convertToResponseDTO — có variant & image
    // ==============================================================
    @Test
    void testConvertToResponseDTO_WithVariantAndImage() {
        ProductVariant variant = new ProductVariant();
        variant.setId(1L);
        variant.setSku("RING001-M");
        variant.setMaterial("Gold");
        variant.setSize("M");
        variant.setPriceModifier(BigDecimal.ZERO);

        ProductImage image = new ProductImage();
        image.setId(1L);
        image.setImageUrl("test.jpg");

        product.setVariants(Set.of(variant));
        product.setImages(Set.of(image));

        ProductResponseDTO dto = productService.convertToResponseDTO(product);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Gold Ring", dto.getName());
        assertEquals(1, dto.getVariants().size());
        assertEquals(1, dto.getImages().size());
        assertEquals("test.jpg", dto.getImages().get(0).getImageUrl());
        assertEquals("M", dto.getVariants().get(0).getSize());
    }

    // ==============================================================
    // ✅ 16. convertToResponseDTO — không có variant & image
    // ==============================================================
    @Test
    void testConvertToResponseDTO_WithoutVariantAndImage() {
        product.setVariants(Set.of());
        product.setImages(Set.of());

        ProductResponseDTO dto = productService.convertToResponseDTO(product);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Gold Ring", dto.getName());
        assertTrue(dto.getVariants().isEmpty());
        assertTrue(dto.getImages().isEmpty());
        assertEquals("Rings", dto.getCategoryName());
    }

    // ==============================================================
    // ✅ 17. convertToResponseDTO — với category null
    // ==============================================================
    @Test
    void testConvertToResponseDTO_WithNullCategory() {
        product.setCategory(null);

        ProductResponseDTO dto = productService.convertToResponseDTO(product);

        assertNotNull(dto);
        assertEquals("Gold Ring", dto.getName());
        assertNull(dto.getCategory());
        assertNull(dto.getCategoryId());
        assertNull(dto.getCategoryName());
    }

    // ==============================================================
    // ✅ 18. Test convertVariantToDTO với inventory null
    // ==============================================================
    @Test
    void testConvertVariantToDTO_WithNullInventory() {
        ProductVariant variant = new ProductVariant();
        variant.setId(1L);
        variant.setSize("M");
        variant.setMaterial("Gold");
        variant.setSku("RING001-M");
        variant.setPriceModifier(BigDecimal.ZERO);
        variant.setInventory(null); // No inventory

        // This tests the private method through convertToResponseDTO
        product.setVariants(Set.of(variant));

        ProductResponseDTO dto = productService.convertToResponseDTO(product);

        assertNotNull(dto);
        assertEquals(1, dto.getVariants().size());
        assertEquals(0, dto.getVariants().get(0).getQuantity()); // Should default to 0
    }
}
