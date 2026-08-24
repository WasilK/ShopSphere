package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.product.ProductRequest;
import com.wasil.ShopSphere.dto.product.ProductResponse;
import com.wasil.ShopSphere.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @PostMapping
    public ProductResponse addProduct(@Valid @RequestBody ProductRequest productRequest){
        return productService.addProduct(productRequest);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest){
        return productService.updateProduct(id, productRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
    }


    @GetMapping
    public Page<ProductResponse> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "prodCreatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return productService.searchAndFilterProducts(
                name,
                minPrice,
                maxPrice,
                categoryId,
                page,
                size,
                sortBy,
                direction
        );
    }
}
