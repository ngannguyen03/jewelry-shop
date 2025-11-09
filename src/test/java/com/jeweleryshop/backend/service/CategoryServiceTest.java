package com.jeweleryshop.backend.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.jeweleryshop.backend.dto.CategoryDTO;
import com.jeweleryshop.backend.entity.Category;
import com.jeweleryshop.backend.exception.DuplicateResourceException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryDTO dto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Rings");
        category.setDescription("Luxury rings");

        dto = new CategoryDTO();
        dto.setId(1L);
        dto.setName("Rings");
        dto.setDescription("Luxury rings");
    }

    // =======================================================
    // ✅ 1. Lấy tất cả danh mục
    // =======================================================
    @Test
    void testGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryDTO> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Rings", result.get(0).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    // =======================================================
    // ✅ 2. Lấy danh mục public
    // =======================================================
    @Test
    void testGetPublicCategories() {
        Category parent = new Category();
        parent.setId(99L);
        category.setParent(parent);

        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryDTO> result = categoryService.getPublicCategories();

        assertEquals(1, result.size());
        assertEquals("Rings", result.get(0).getName());
        assertEquals(99L, result.get(0).getParent_id());
    }

    // =======================================================
    // ✅ 3. Lấy danh mục theo ID
    // =======================================================
    @Test
    void testGetCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDTO result = categoryService.getCategoryById(1L);

        assertEquals("Rings", result.getName());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void testGetCategoryById_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    // =======================================================
    // ✅ 4. Tạo danh mục mới
    // =======================================================
    @Test
    void testCreateCategory_Success() {
        dto.setParent_id(null);
        when(categoryRepository.findByName("Rings")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO result = categoryService.createCategory(dto);

        assertEquals("Rings", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testCreateCategory_DuplicateName() {
        when(categoryRepository.findByName("Rings")).thenReturn(Optional.of(category));
        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(dto));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void testCreateCategory_WithParent() {
        dto.setParent_id(2L);
        Category parent = new Category();
        parent.setId(2L);

        when(categoryRepository.findByName("Rings")).thenReturn(Optional.empty());
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO result = categoryService.createCategory(dto);

        assertNotNull(result);
        verify(categoryRepository).findById(2L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testCreateCategory_ParentNotFound() {
        dto.setParent_id(99L);
        when(categoryRepository.findByName("Rings")).thenReturn(Optional.empty());
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.createCategory(dto));
    }

    // =======================================================
    // ✅ 5. Cập nhật danh mục
    // =======================================================
    @Test
    void testUpdateCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Rings")).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO result = categoryService.updateCategory(1L, dto);

        assertEquals("Rings", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(1L, dto));
    }

    @Test
    void testUpdateCategory_DuplicateName() {
        Category another = new Category();
        another.setId(2L);
        another.setName("Rings");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Rings")).thenReturn(Optional.of(another));

        assertThrows(DuplicateResourceException.class, () -> categoryService.updateCategory(1L, dto));
    }

    @Test
    void testUpdateCategory_WithParent() {
        Category parent = new Category();
        parent.setId(99L);
        dto.setParent_id(99L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Rings")).thenReturn(Optional.of(category));
        when(categoryRepository.findById(99L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDTO result = categoryService.updateCategory(1L, dto);
        assertNotNull(result);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_ParentNotFound() {
        dto.setParent_id(999L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Rings")).thenReturn(Optional.of(category));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(1L, dto));
    }

    // =======================================================
    // ✅ 6. Xóa danh mục
    // =======================================================
    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void testDeleteCategory_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(1L));
    }
}
