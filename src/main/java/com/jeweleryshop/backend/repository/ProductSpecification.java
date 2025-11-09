package com.jeweleryshop.backend.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.jeweleryshop.backend.entity.Category;
import com.jeweleryshop.backend.entity.Product;
import com.jeweleryshop.backend.entity.ProductVariant;

import jakarta.persistence.criteria.Join;

public class ProductSpecification {

    public static Specification<Product> hasCategory(Long categoryId) {
        if (categoryId == null) {
            return null; // Không lọc nếu không có categoryId
        }
        return (root, query, criteriaBuilder) -> {
            Join<Product, Category> categoryJoin = root.join("category");
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<Product> hasPriceGreaterThanOrEqual(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), minPrice);
    }

    public static Specification<Product> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), maxPrice);
    }

    public static Specification<Product> hasNameLike(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> hasVariantWithSize(String size) {
        if (size == null || size.trim().isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            // Use a subquery to avoid duplicate products in the result
            query.distinct(true);
            Join<Product, ProductVariant> variants = root.join("variants");
            return criteriaBuilder.equal(variants.get("size"), size);
        };
    }

    public static Specification<Product> hasVariantWithMaterial(String material) {
        if (material == null || material.trim().isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            Join<Product, ProductVariant> variants = root.join("variants");
            return criteriaBuilder.equal(variants.get("material"), material);
        };
    }
}
