package com.jeweleryshop.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jeweleryshop.backend.dto.ProductRequestDTO;
import com.jeweleryshop.backend.dto.ProductResponseDTO;
import com.jeweleryshop.backend.service.ProductService;

import jakarta.validation.Valid;

/**
 * 🧩 ProductController — Public + Admin + HATEOAS
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ============================================================
    // 🌍 1️⃣ API PUBLIC - Dành cho React FE (Home page)
    // ============================================================
    /**
     * ✅ Trả về mảng JSON thuần — React gọi fetch(...).then(r => r.json()) dùng
     * được ngay .slice(), .map() mà KHÔNG cần .data
     */
    @GetMapping("/public/products")
    public List<ProductResponseDTO> getPublicProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String material
    ) {
        return productService
                .getAllProducts(Pageable.unpaged(), categoryId, minPrice, maxPrice, name, size, material)
                .getContent(); // ✅ Trả mảng trực tiếp
    }

    // ============================================================
    // 🔹 2️⃣ Danh sách sản phẩm (Public + HATEOAS)
    // ============================================================
    @GetMapping("/products")
    public CollectionModel<EntityModel<ProductResponseDTO>> getAllProducts(
            Pageable pageable,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String material
    ) {
        Page<ProductResponseDTO> page = productService.getAllProducts(pageable, categoryId, minPrice, maxPrice, name, size, material);

        List<EntityModel<ProductResponseDTO>> models = page.stream()
                .map(p -> EntityModel.of(p,
                linkTo(methodOn(ProductController.class).getProductById(p.getId())).withSelfRel(),
                linkTo(methodOn(ProductController.class)
                        .getAllProducts(pageable, null, null, null, null, null, null)).withRel("all-products"),
                linkTo(methodOn(CategoryController.class)
                        .getCategoryById(p.getCategoryId())).withRel("category")))
                .collect(Collectors.toList());

        return CollectionModel.of(models,
                linkTo(methodOn(ProductController.class)
                        .getAllProducts(pageable, null, null, null, null, null, null)).withSelfRel());
    }

    // ============================================================
    // 🔹 3️⃣ Chi tiết sản phẩm (Public + HATEOAS)
    // ============================================================
    @GetMapping("/products/{id}")
    public EntityModel<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);

        return EntityModel.of(product,
                linkTo(methodOn(ProductController.class).getProductById(id)).withSelfRel(),
                linkTo(methodOn(ProductController.class)
                        .getAllProducts(Pageable.unpaged(), null, null, null, null, null, null)).withRel("all-products"),
                linkTo(methodOn(CategoryController.class)
                        .getCategoryById(product.getCategoryId())).withRel("category"));
    }

    // ============================================================
    // 🔒 4️⃣ ADMIN CRUD
    // ============================================================
    @PostMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDTO createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        return productService.createProduct(requestDTO);
    }

    @PutMapping("/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDTO updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO requestDTO
    ) {
        return productService.updateProduct(id, requestDTO);
    }

    @DeleteMapping("/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductResponseDTO> getAllProductsForAdmin() {
        return productService.getAllProductsForAdmin();
    }
}
