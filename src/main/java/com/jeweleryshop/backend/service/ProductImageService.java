package com.jeweleryshop.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.jeweleryshop.backend.dto.ProductImageDTO;
import com.jeweleryshop.backend.entity.Product;
import com.jeweleryshop.backend.entity.ProductImage;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.ProductImageRepository;
import com.jeweleryshop.backend.repository.ProductRepository;

@Service
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;

    public ProductImageService(ProductRepository productRepository, ProductImageRepository productImageRepository, FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ProductImageDTO addImageToProduct(Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        String fileName = fileStorageService.storeFile(file);

        // Construct the file download URI
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/images/")
                .path(fileName)
                .toUriString();

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageUrl(fileDownloadUri);

        ProductImage savedImage = productImageRepository.save(productImage);

        return new ProductImageDTO(savedImage.getId(), savedImage.getImageUrl());
    }

    // You can add a method to delete an image here
}
