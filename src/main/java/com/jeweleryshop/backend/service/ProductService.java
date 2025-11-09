package com.jeweleryshop.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.dto.CategoryDTO;
import com.jeweleryshop.backend.dto.ProductImageDTO;
import com.jeweleryshop.backend.dto.ProductRequestDTO;
import com.jeweleryshop.backend.dto.ProductResponseDTO;
import com.jeweleryshop.backend.dto.ProductVariantDTO;
import com.jeweleryshop.backend.entity.Category;
import com.jeweleryshop.backend.entity.Product;
import com.jeweleryshop.backend.entity.ProductVariant;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.CategoryRepository;
import com.jeweleryshop.backend.repository.ProductRepository;
import com.jeweleryshop.backend.repository.ProductSpecification;
import com.jeweleryshop.backend.repository.ProductVariantRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;

    public ProductService(ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
    }

    // ==============================================================
    // ✅ Tạo sản phẩm mới — tự động tạo biến thể mặc định
    // ==============================================================
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found with id: " + requestDTO.getCategoryId()));

        Product product = new Product();
        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setBasePrice(requestDTO.getBasePrice());
        product.setDiscountPrice(requestDTO.getDiscountPrice());
        product.setSkuPrefix(requestDTO.getSkuPrefix());
        product.setIsActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true);

        // ✅ THÊM: Set imageUrl từ request
        product.setImageUrl(requestDTO.getImageUrl());

        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        // ✅ Nếu sản phẩm chưa có biến thể thì tự tạo biến thể mặc định
        if (savedProduct.getVariants() == null || savedProduct.getVariants().isEmpty()) {
            ProductVariant defaultVariant = new ProductVariant();
            defaultVariant.setProduct(savedProduct);
            defaultVariant.setSku(savedProduct.getSkuPrefix() + "-DEFAULT");
            defaultVariant.setMaterial("Default");
            defaultVariant.setSize("Default");
            defaultVariant.setPriceModifier(BigDecimal.ZERO);
            productVariantRepository.save(defaultVariant);
        }

        return convertToResponseDTO(savedProduct);
    }

    // ==============================================================
    // ✅ Lấy tất cả sản phẩm (Public, có lọc + phân trang)
    // ==============================================================
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            String name, String size, String material) {

        Specification<Product> spec = Specification.where(ProductSpecification.hasCategory(categoryId))
                .and(ProductSpecification.hasPriceGreaterThanOrEqual(minPrice))
                .and(ProductSpecification.hasPriceLessThanOrEqual(maxPrice))
                .and(ProductSpecification.hasNameLike(name))
                .and(ProductSpecification.hasVariantWithSize(size))
                .and(ProductSpecification.hasVariantWithMaterial(material));

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productPage.map(this::convertToResponseDTO);
    }

    // ==============================================================
    // ✅ Lấy danh sách sản phẩm cho ADMIN
    // ==============================================================
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProductsForAdmin() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==============================================================
    // ✅ Lấy chi tiết sản phẩm theo ID
    // ==============================================================
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToResponseDTO(product);
    }

    // ==============================================================
    // ✅ Cập nhật sản phẩm
    // ==============================================================
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO requestDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found with id: " + requestDTO.getCategoryId()));

        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setBasePrice(requestDTO.getBasePrice());
        product.setDiscountPrice(requestDTO.getDiscountPrice());
        product.setSkuPrefix(requestDTO.getSkuPrefix());
        product.setIsActive(requestDTO.getIsActive());

        // ✅ THÊM: Update imageUrl từ request
        product.setImageUrl(requestDTO.getImageUrl());

        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);
        return convertToResponseDTO(updatedProduct);
    }

    // ==============================================================
    // ✅ Xóa sản phẩm
    // ==============================================================
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // ==============================================================
    // ✅ Chuyển Entity → DTO (chuẩn cho HATEOAS + React)
    // ==============================================================
    public ProductResponseDTO convertToResponseDTO(Product product) {
        List<ProductImageDTO> imageDTOs = (product.getImages() != null)
                ? product.getImages().stream()
                        .map(img -> new ProductImageDTO(img.getId(), img.getImageUrl()))
                        .collect(Collectors.toList())
                : List.of();

        List<ProductVariantDTO> variantDTOs = (product.getVariants() != null)
                ? product.getVariants().stream()
                        .map(this::convertVariantToDTO)
                        .collect(Collectors.toList())
                : List.of();

        // ✅ CategoryDTO để gắn vào ProductResponseDTO
        Category category = product.getCategory();
        CategoryDTO categoryDTO = category != null
                ? new CategoryDTO(category.getId(), category.getName(), category.getDescription(),
                        category.getParent() != null ? category.getParent().getId() : null)
                : null;

        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBasePrice(product.getBasePrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setSkuPrefix(product.getSkuPrefix());
        dto.setIsActive(product.getIsActive());

        // ✅ THÊM: Set imageUrl
        dto.setImageUrl(product.getImageUrl());

        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCategory(categoryDTO);
        dto.setCategoryId(category != null ? category.getId() : null);
        dto.setCategoryName(category != null ? category.getName() : null);
        dto.setImages(imageDTOs);
        dto.setVariants(variantDTOs);

        return dto;
    }

    private ProductVariantDTO convertVariantToDTO(ProductVariant variant) {
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
