package com.jeweleryshop.backend.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeweleryshop.backend.dto.ProductRequestDTO;
import com.jeweleryshop.backend.dto.ProductResponseDTO;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponseDTO productResponse;
    private ProductRequestDTO productRequest;

    @BeforeEach
    void setUp() {
        productResponse = new ProductResponseDTO();
        productResponse.setId(1L);
        productResponse.setName("Diamond Ring");
        productResponse.setDescription("Beautiful diamond ring");
        productResponse.setBasePrice(new BigDecimal("999.99"));
        productResponse.setDiscountPrice(new BigDecimal("899.99"));
        productResponse.setCategoryId(1L);
        productResponse.setCategoryName("Rings");
        productResponse.setIsActive(true);

        productRequest = new ProductRequestDTO();
        productRequest.setName("Diamond Ring");
        productRequest.setDescription("Beautiful diamond ring");
        productRequest.setBasePrice(new BigDecimal("999.99"));
        productRequest.setDiscountPrice(new BigDecimal("899.99"));
        productRequest.setCategoryId(1L);
        productRequest.setIsActive(true);
        productRequest.setSkuPrefix("RING001");
    }

    // ============================================================
    // ✅ PUBLIC API TESTS (No authentication required)
    // ============================================================
    @Test
    void getAllProducts_ShouldReturnProductsWithLinks() throws Exception {
        // Arrange
        Page<ProductResponseDTO> page = new PageImpl<>(List.of(productResponse));
        when(productService.getAllProducts(any(Pageable.class), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                // ✅ SỬA: Sử dụng _embedded thay vì content
                .andExpect(jsonPath("$._embedded.productResponseDTOList[0].id").value(1))
                .andExpect(jsonPath("$._embedded.productResponseDTOList[0].name").value("Diamond Ring"))
                .andExpect(jsonPath("$._embedded.productResponseDTOList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._embedded.productResponseDTOList[0]._links.all-products.href").exists())
                .andExpect(jsonPath("$._embedded.productResponseDTOList[0]._links.category.href").exists());
    }

    @Test
    void getProductById_ShouldReturnProductWithLinks() throws Exception {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(productResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Diamond Ring"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.all-products.href").exists())
                .andExpect(jsonPath("$._links.category.href").exists());
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldReturn404() throws Exception {
        // Arrange
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    void getPublicProducts_ShouldReturnProductList() throws Exception {
        // Arrange
        Page<ProductResponseDTO> page = new PageImpl<>(List.of(productResponse));
        when(productService.getAllProducts(any(Pageable.class), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                // ✅ SỬA: API public có thể trả về array trực tiếp
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Diamond Ring"));
    }

    // ============================================================
    // ✅ ADMIN API TESTS (With authentication)
    // ============================================================
    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_AsAdmin_ShouldReturnCreatedProduct() throws Exception {
        // Arrange
        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(productResponse);

        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Diamond Ring"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_AsUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        // Arrange
        when(productService.updateProduct(eq(1L), any(ProductRequestDTO.class))).thenReturn(productResponse);

        // Act & Assert
        mockMvc.perform(put("/api/admin/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Diamond Ring"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllProductsForAdmin_ShouldReturnProductList() throws Exception {
        // Arrange
        when(productService.getAllProductsForAdmin()).thenReturn(List.of(productResponse));

        // Act & Assert
        mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isOk())
                // ✅ SỬA: API admin có thể trả về array trực tiếp
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Diamond Ring"));
    }
}
