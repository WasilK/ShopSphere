package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.product.CategoryResponse;
import com.wasil.ShopSphere.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.wasil.ShopSphere.dto.product.CategoryRequest;

import java.util.List;
@Tag(name = "Category API's")
@RestController
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @Operation(summary = "Gets all the categories.")
    @GetMapping
    public List<CategoryResponse> getAllCategories(){
        return categoryService.getAllCategories();
    }
    @Operation(summary = "Gets a specific category by category id.")
    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable Long id){
        return categoryService.getCategoryById(id);
    }
    @Operation(summary = "Creates a category only allowed for admin.")
    @PostMapping
    public CategoryResponse createCategory(@RequestBody @Valid CategoryRequest categoryRequest){
        return categoryService.createCategory(categoryRequest);
    }
    @Operation(summary = "Updates a category using category id, only allowed for admin.")
    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request){
        return categoryService.updateCategory(id, request);
    }
    @Operation(summary = "Deactivates a category using category id, only allowed for admin")
    @DeleteMapping("/{id}")
    public CategoryResponse deleteCategory(@PathVariable Long id){
        return categoryService.deleteCategory(id);
    }

    @Operation(summary = "Updates a category status using category id, only allowed for admin.")
    @PutMapping("/{id}/status")
    public void updateCategoryStatus(@PathVariable Long id){
        categoryService.updateCategoryStatus(id);
    }
}
