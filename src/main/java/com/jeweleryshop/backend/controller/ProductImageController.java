package com.jeweleryshop.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jeweleryshop.backend.dto.ProductImageDTO;
import com.jeweleryshop.backend.service.ProductImageService;

@RestController
@RequestMapping("/api/admin/products/{productId}/images")
@PreAuthorize("hasRole('ADMIN')")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @PostMapping
    public ResponseEntity<ProductImageDTO> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        ProductImageDTO imageDTO = productImageService.addImageToProduct(productId, file);
        return new ResponseEntity<>(imageDTO, HttpStatus.CREATED);
    }

    // You can add an endpoint to delete an image by its ID
    // @DeleteMapping("/{imageId}")
    // public ResponseEntity<Void> deleteImage(...)
}
