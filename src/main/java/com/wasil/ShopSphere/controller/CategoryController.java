package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.product.CategoryResponse;
import com.wasil.ShopSphere.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.wasil.ShopSphere.dto.product.CategoryRequest;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @GetMapping
    public List<CategoryResponse> getAllCategories(){
        return categoryService.getAllCategories();
    }
    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable Long id){
        return categoryService.getCategoryById(id);
    }
    @PostMapping
    public CategoryResponse createCategory(@RequestBody @Valid CategoryRequest categoryRequest){
        return categoryService.createCategory(categoryRequest);
    }
    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request){
        return categoryService.updateCategory(id, request);
    }
    @DeleteMapping("/{id}")
    public CategoryResponse deleteCategory(@PathVariable Long id){
        return categoryService.deleteCategory(id);
    }

    @PutMapping("/{id}/status")
    public void updateCategoryStatus(@PathVariable Long id){
        categoryService.updateCategoryStatus(id);
    }
}
