package com.jeweleryshop.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.dto.ProductVariantDTO;
import com.jeweleryshop.backend.entity.Inventory;
import com.jeweleryshop.backend.entity.Product;
import com.jeweleryshop.backend.entity.ProductVariant;
import com.jeweleryshop.backend.exception.DuplicateResourceException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.ProductRepository;
import com.jeweleryshop.backend.repository.ProductVariantRepository;

@Service
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    public ProductVariantService(ProductVariantRepository variantRepository, ProductRepository productRepository) {
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
    }

    // ============================================================
    // ✅ 1. Lấy danh sách biến thể theo sản phẩm
    // ============================================================
    @Transactional(readOnly = true)
    public List<ProductVariantDTO> getVariantsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Lấy danh sách biến thể từ product và chuyển sang DTO
        return product.getVariants()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // ✅ 2. Thêm biến thể mới cho sản phẩm
    // ============================================================
    @Transactional
    public ProductVariantDTO addVariantToProduct(Long productId, ProductVariantDTO variantDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (variantRepository.existsBySku(variantDTO.getSku())) {
            throw new DuplicateResourceException("SKU '" + variantDTO.getSku() + "' already exists.");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSize(variantDTO.getSize());
        variant.setMaterial(variantDTO.getMaterial());
        variant.setPriceModifier(variantDTO.getPriceModifier());
        variant.setSku(variantDTO.getSku());

        // ✅ Tạo mới Inventory cho biến thể
        Inventory inventory = new Inventory();
        inventory.setQuantity(variantDTO.getQuantity());
        inventory.setVariant(variant); // liên kết ngược

        variant.setInventory(inventory);

        ProductVariant savedVariant = variantRepository.save(variant);
        return convertToDTO(savedVariant);
    }

    // ============================================================
    // ✅ 3. Cập nhật biến thể
    // ============================================================
    @Transactional
    public ProductVariantDTO updateVariant(Long variantId, ProductVariantDTO variantDTO) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        // Kiểm tra SKU trùng lặp
        if (variantDTO.getSku() != null && !variantDTO.getSku().equals(variant.getSku())) {
            if (variantRepository.existsBySku(variantDTO.getSku())) {
                throw new DuplicateResourceException("SKU '" + variantDTO.getSku() + "' already exists.");
            }
            variant.setSku(variantDTO.getSku());
        }

        // Cập nhật thông tin khác
        variant.setSize(variantDTO.getSize());
        variant.setMaterial(variantDTO.getMaterial());
        variant.setPriceModifier(variantDTO.getPriceModifier());

        // Cập nhật tồn kho
        if (variant.getInventory() != null && variantDTO.getQuantity() != null) {
            variant.getInventory().setQuantity(variantDTO.getQuantity());
        }

        return convertToDTO(variantRepository.save(variant));
    }

    // ============================================================
    // ✅ 4. Xóa biến thể
    // ============================================================
    @Transactional
    public void deleteVariant(Long variantId) {
        if (!variantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("Variant not found with id: " + variantId);
        }
        variantRepository.deleteById(variantId);
    }

    // ============================================================
    // ✅ 5. Hàm chuyển đổi Entity → DTO
    // ============================================================
    private ProductVariantDTO convertToDTO(ProductVariant variant) {
        int quantity = (variant.getInventory() != null)
                ? variant.getInventory().getQuantity()
                : 0;
        return new ProductVariantDTO(
                variant.getId(),
                variant.getSize(),
                variant.getMaterial(),
                variant.getPriceModifier(),
                variant.getSku(),
                quantity
        );
    }
}
