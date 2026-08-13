package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.services.ProductService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @PostMapping("/products/addProduct")
    public Product addProduct(@RequestBody Product product){
        return productService.addProduct(product);
    }
}
