package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.product.CategoryRequest;
import com.wasil.ShopSphere.dto.product.CategoryResponse;
import com.wasil.ShopSphere.exceptions.CategoryNotFoundException;
import com.wasil.ShopSphere.exceptions.DuplicateResourceException;
import com.wasil.ShopSphere.model.Category;
import org.springframework.stereotype.Service;
import com.wasil.ShopSphere.repositories.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(CategoryRequest categoryRequest){
        Category category = new Category();
        if(categoryRepository.findByCategoryName(categoryRequest.getCategoryName()).isPresent()){
            throw new DuplicateResourceException("Category already exists");
        }
        category.setCategoryName(categoryRequest.getCategoryName());
        category.setCategoryIsActive(true);
        categoryRepository.save(category);
        return convertToResponse(category);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest){
        Category category = categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found."));
        category.setCategoryName(categoryRequest.getCategoryName());
        Category savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }
    public CategoryResponse deleteCategory(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found."));
        category.setCategoryIsActive(false);
        Category savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    public void updateCategoryStatus(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found."));
        category.setCategoryIsActive(true);
        categoryRepository.save(category);
    }
    public CategoryResponse getCategoryById(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found."));
        return convertToResponse(category);
    }
    public List<CategoryResponse> getAllCategories(){
        return categoryRepository.findAll().stream().map(this::convertToResponse).toList();
    }

    private CategoryResponse convertToResponse(Category category){
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setCategoryId(category.getCategoryId());
        categoryResponse.setCategoryName(category.getCategoryName());
        categoryResponse.setCategoryIsActive(category.getCategoryIsActive());
        return categoryResponse;
    }
}
