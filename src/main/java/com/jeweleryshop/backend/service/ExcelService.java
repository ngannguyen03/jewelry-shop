package com.jeweleryshop.backend.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jeweleryshop.backend.entity.Category;
import com.jeweleryshop.backend.entity.Product;
import com.jeweleryshop.backend.repository.CategoryRepository;
import com.jeweleryshop.backend.repository.ProductRepository;
import com.jeweleryshop.backend.utils.ExcelHelper;

@Service
public class ExcelService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public ByteArrayInputStream exportProductsToExcel() {
        List<Product> products = productRepository.findAll();
        List<Category> categories = categoryRepository.findAll();
        return ExcelHelper.productsAndCategoriesToExcel(products, categories);
    }

    public void importProductsFromExcel(MultipartFile file) {
        try {
            List<Category> categories = categoryRepository.findAll();
            List<Product> products = ExcelHelper.excelToProducts(file.getInputStream(), categories);

            int successCount = 0;
            int updateCount = 0;

            // Save products
            for (Product product : products) {
                // Check if product exists (by name)
                Product existingProduct = productRepository.findByName(product.getName())
                        .orElse(null);

                if (existingProduct != null) {
                    // Update existing product
                    existingProduct.setDescription(product.getDescription());
                    existingProduct.setBasePrice(product.getBasePrice());
                    existingProduct.setDiscountPrice(product.getDiscountPrice());
                    existingProduct.setSkuPrefix(product.getSkuPrefix());
                    existingProduct.setCategory(product.getCategory());
                    existingProduct.setIsActive(product.getIsActive());
                    existingProduct.setImageUrl(product.getImageUrl());
                    productRepository.save(existingProduct);
                    updateCount++;
                } else {
                    // Create new product
                    productRepository.save(product);
                    successCount++;
                }
            }

            // Log kết quả import
            System.out.println("Import thành công: " + successCount + " sản phẩm mới, " + updateCount + " sản phẩm được cập nhật");

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi import file Excel: " + e.getMessage());
        }
    }

    /**
     * Phương thức để lấy số lượng sản phẩm đã import (dùng cho controller)
     */
    public int getImportedCount(MultipartFile file) {
        try {
            List<Category> categories = categoryRepository.findAll();
            List<Product> products = ExcelHelper.excelToProducts(file.getInputStream(), categories);
            return products.size();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đếm sản phẩm từ file Excel: " + e.getMessage());
        }
    }
}
