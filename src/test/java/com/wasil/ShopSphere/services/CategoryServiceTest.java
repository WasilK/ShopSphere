package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.product.CategoryRequest;
import com.wasil.ShopSphere.dto.product.CategoryResponse;
import com.wasil.ShopSphere.exceptions.CategoryNotFoundException;
import com.wasil.ShopSphere.exceptions.DuplicateResourceException;
import com.wasil.ShopSphere.model.Category;
import com.wasil.ShopSphere.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {

        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");
        category.setCategoryIsActive(true);

        categoryRequest = new CategoryRequest();
        categoryRequest.setCategoryName("Electronics");
    }


    // =========================================================
    // CREATE CATEGORY
    // =========================================================

    @Test
    void shouldCreateCategorySuccessfully() {

        // Arrange
        when(categoryRepository.findByCategoryName("Electronics"))
                .thenReturn(Optional.empty());

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category savedCategory = invocation.getArgument(0);
                    savedCategory.setCategoryId(1L);
                    return savedCategory;
                });

        // Act
        CategoryResponse response =
                categoryService.createCategory(categoryRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getCategoryId());
        assertEquals("Electronics", response.getCategoryName());
        assertTrue(response.getCategoryIsActive());

        verify(categoryRepository).findByCategoryName("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }


    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {

        // Arrange
        when(categoryRepository.findByCategoryName("Electronics"))
                .thenReturn(Optional.of(category));

        // Act & Assert
        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> categoryService.createCategory(categoryRequest)
                );

        assertEquals("Category already exists", exception.getMessage());

        verify(categoryRepository).findByCategoryName("Electronics");

        // Category must not be saved
        verify(categoryRepository, never()).save(any(Category.class));
    }


    // =========================================================
    // UPDATE CATEGORY
    // =========================================================

    @Test
    void shouldUpdateCategorySuccessfully() {

        // Arrange
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(category);

        CategoryRequest request = new CategoryRequest();
        request.setCategoryName("Mobile Phones");

        // Act
        CategoryResponse response =
                categoryService.updateCategory(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getCategoryId());
        assertEquals("Mobile Phones", response.getCategoryName());
        assertTrue(response.getCategoryIsActive());

        assertEquals("Mobile Phones", category.getCategoryName());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(category);
    }


    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingCategory() {

        // Arrange
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        CategoryNotFoundException exception =
                assertThrows(
                        CategoryNotFoundException.class,
                        () -> categoryService.updateCategory(
                                1L,
                                categoryRequest
                        )
                );

        assertEquals("Category not found.", exception.getMessage());

        verify(categoryRepository).findById(1L);

        verify(categoryRepository, never())
                .save(any(Category.class));
    }


    // =========================================================
    // DELETE CATEGORY / SOFT DELETE
    // =========================================================

    @Test
    void shouldSoftDeleteCategorySuccessfully() {

        // Arrange
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(category);

        // Act
        CategoryResponse response =
                categoryService.deleteCategory(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getCategoryId());
        assertEquals("Electronics", response.getCategoryName());

        // Category should become inactive
        assertFalse(response.getCategoryIsActive());
        assertFalse(category.getCategoryIsActive());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(category);
    }


    @Test
    void shouldThrowExceptionWhenDeletingNonExistingCategory() {

        // Arrange
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        CategoryNotFoundException exception =
                assertThrows(
                        CategoryNotFoundException.class,
                        () -> categoryService.deleteCategory(1L)
                );

        assertEquals("Category not found.", exception.getMessage());

        verify(categoryRepository).findById(1L);

        verify(categoryRepository, never())
                .save(any(Category.class));
    }


    // =========================================================
    // UPDATE CATEGORY STATUS / REACTIVATE
    // =========================================================

    @Test
    void shouldReactivateCategorySuccessfully() {

        // Arrange
        category.setCategoryIsActive(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(category);

        // Act
        categoryService.updateCategoryStatus(1L);

        // Assert
        assertTrue(category.getCategoryIsActive());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(category);
    }


    @Test
    void shouldThrowExceptionWhenUpdatingStatusOfNonExistingCategory() {

        // Arrange
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        CategoryNotFoundException exception =
                assertThrows(
                        CategoryNotFoundException.class,
                        () -> categoryService.updateCategoryStatus(1L)
                );

        assertEquals("Category not found.", exception.getMessage());

        verify(categoryRepository).findById(1L);

        verify(categoryRepository, never())
                .save(any(Category.class));
    }


    // =========================================================
    // GET CATEGORY BY ID
    // =========================================================

    @Test
    void shouldGetCategoryByIdSuccessfully() {

        // Arrange
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // Act
        CategoryResponse response =
                categoryService.getCategoryById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getCategoryId());
        assertEquals("Electronics", response.getCategoryName());
        assertTrue(response.getCategoryIsActive());

        verify(categoryRepository).findById(1L);
    }


    @Test
    void shouldThrowExceptionWhenGettingNonExistingCategory() {

        // Arrange
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        CategoryNotFoundException exception =
                assertThrows(
                        CategoryNotFoundException.class,
                        () -> categoryService.getCategoryById(1L)
                );

        assertEquals("Category not found.", exception.getMessage());

        verify(categoryRepository).findById(1L);
    }


    // =========================================================
    // GET ALL CATEGORIES
    // =========================================================

    @Test
    void shouldGetAllCategoriesSuccessfully() {

        // Arrange
        Category category2 = new Category();
        category2.setCategoryId(2L);
        category2.setCategoryName("Clothing");
        category2.setCategoryIsActive(true);

        when(categoryRepository.findAll())
                .thenReturn(List.of(category, category2));

        // Act
        List<CategoryResponse> responses =
                categoryService.getAllCategories();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).getCategoryId());
        assertEquals("Electronics", responses.get(0).getCategoryName());
        assertTrue(responses.get(0).getCategoryIsActive());

        assertEquals(2L, responses.get(1).getCategoryId());
        assertEquals("Clothing", responses.get(1).getCategoryName());
        assertTrue(responses.get(1).getCategoryIsActive());

        verify(categoryRepository).findAll();
    }


    @Test
    void shouldReturnEmptyListWhenNoCategoriesExist() {

        // Arrange
        when(categoryRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<CategoryResponse> responses =
                categoryService.getAllCategories();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(categoryRepository).findAll();
    }
}
