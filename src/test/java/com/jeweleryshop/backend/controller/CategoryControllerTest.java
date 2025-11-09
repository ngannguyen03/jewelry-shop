package com.jeweleryshop.backend.controller;

import java.time.LocalDateTime;
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
import com.jeweleryshop.backend.dto.CategoryDTO;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.service.CategoryService;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("Rings");
        categoryDTO.setDescription("Beautiful rings collection");
        categoryDTO.setParentId(null); // No parent category
        categoryDTO.setCreatedAt(LocalDateTime.now());
        categoryDTO.setUpdatedAt(LocalDateTime.now());
    }

    // ============================================================
    // ✅ PUBLIC API TESTS (No authentication required)
    // ============================================================
    @Test
    void getPublicCategories_ShouldReturnCategoryList() throws Exception {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryDTO));

        // Act & Assert
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Rings"))
                .andExpect(jsonPath("$[0].description").value("Beautiful rings collection"))
                .andExpect(jsonPath("$[0].parentId").isEmpty());
    }

    @Test
    void getAllCategories_ShouldReturnCategoryList() throws Exception {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryDTO));

        // Act & Assert
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Rings"))
                .andExpect(jsonPath("$[0].description").value("Beautiful rings collection"))
                .andExpect(jsonPath("$[0].parentId").isEmpty());
    }

    @Test
    void getCategoryById_ShouldReturnCategory() throws Exception {
        // Arrange
        when(categoryService.getCategoryById(1L)).thenReturn(categoryDTO);

        // Act & Assert
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rings"))
                .andExpect(jsonPath("$.description").value("Beautiful rings collection"))
                .andExpect(jsonPath("$.parentId").isEmpty());
    }

    @Test
    void getCategoryById_WhenCategoryNotFound_ShouldReturn404() throws Exception {
        // Arrange
        when(categoryService.getCategoryById(999L))
                .thenThrow(new ResourceNotFoundException("Category not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Category not found with id: 999"));
    }

    // ============================================================
    // ✅ ADMIN API TESTS (With authentication)
    // ============================================================
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCategoriesForAdmin_ShouldReturnCategoryList() throws Exception {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryDTO));

        // Act & Assert
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Rings"))
                .andExpect(jsonPath("$[0].description").value("Beautiful rings collection"))
                .andExpect(jsonPath("$[0].parentId").isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCategoriesForAdmin_AsUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllCategoriesForAdmin_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_AsAdmin_ShouldReturnCreatedCategory() throws Exception {
        // Arrange
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(categoryDTO);

        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rings"))
                .andExpect(jsonPath("$.description").value("Beautiful rings collection"))
                .andExpect(jsonPath("$.parentId").isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_AsUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategory_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_ShouldReturnUpdatedCategory() throws Exception {
        // Arrange
        when(categoryService.updateCategory(eq(1L), any(CategoryDTO.class))).thenReturn(categoryDTO);

        // Act & Assert
        mockMvc.perform(put("/api/admin/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rings"))
                .andExpect(jsonPath("$.description").value("Beautiful rings collection"))
                .andExpect(jsonPath("$.parentId").isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCategory_AsUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/admin/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(categoryService).deleteCategory(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCategory_AsUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/admin/categories/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCategory_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/admin/categories/1"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // ✅ VALIDATION TESTS
    // ============================================================
    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange - Create invalid category (empty name)
        CategoryDTO invalidCategory = new CategoryDTO();
        invalidCategory.setName(""); // Empty name should fail validation
        invalidCategory.setDescription("Test description");

        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCategory)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_WhenCategoryNotFound_ShouldReturn404() throws Exception {
        // Arrange
        when(categoryService.updateCategory(eq(999L), any(CategoryDTO.class)))
                .thenThrow(new ResourceNotFoundException("Category not found with id: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/admin/categories/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Category not found with id: 999"));
    }

    // ============================================================
    // ✅ TEST WITH PARENT CATEGORY
    // ============================================================
    @Test
    void getCategoryWithParent_ShouldReturnCategoryWithParentId() throws Exception {
        // Arrange - Create category with parent
        CategoryDTO categoryWithParent = new CategoryDTO();
        categoryWithParent.setId(2L);
        categoryWithParent.setName("Wedding Rings");
        categoryWithParent.setDescription("Wedding rings collection");
        categoryWithParent.setParentId(1L); // Parent is Rings category

        when(categoryService.getCategoryById(2L)).thenReturn(categoryWithParent);

        // Act & Assert
        mockMvc.perform(get("/api/categories/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Wedding Rings"))
                .andExpect(jsonPath("$.parentId").value(1));
    }
}
