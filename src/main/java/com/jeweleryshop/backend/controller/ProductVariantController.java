package com.jeweleryshop.backend.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.jeweleryshop.backend.dto.ProductVariantDTO;
import com.jeweleryshop.backend.service.ProductVariantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/products/{productId}/variants")
@PreAuthorize("hasRole('ADMIN')")
public class ProductVariantController {

    private final ProductVariantService variantService;

    public ProductVariantController(ProductVariantService variantService) {
        this.variantService = variantService;
    }

    // =============================================================
    // ✅ Lấy danh sách biến thể theo sản phẩm (GET)
    // =============================================================
    @GetMapping
    public ResponseEntity<List<ProductVariantDTO>> getVariantsByProduct(@PathVariable Long productId) {
        List<ProductVariantDTO> variants = variantService.getVariantsByProduct(productId);
        return ResponseEntity.ok(variants);
    }

    // =============================================================
    // ✅ Thêm biến thể mới vào sản phẩm
    // =============================================================
    @PostMapping
    public ResponseEntity<ProductVariantDTO> addVariant(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantDTO variantDTO) {
        ProductVariantDTO createdVariant = variantService.addVariantToProduct(productId, variantDTO);
        return new ResponseEntity<>(createdVariant, HttpStatus.CREATED);
    }

    // =============================================================
    // ✅ Cập nhật biến thể
    // =============================================================
    @PutMapping("/{variantId}")
    public ResponseEntity<ProductVariantDTO> updateVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantDTO variantDTO) {
        ProductVariantDTO updatedVariant = variantService.updateVariant(variantId, variantDTO);
        return ResponseEntity.ok(updatedVariant);
    }

    // =============================================================
    // ✅ Xóa biến thể
    // =============================================================
    @DeleteMapping("/{variantId}")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        variantService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }
}
