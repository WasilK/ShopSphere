package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.repositories.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    ProductRepository prodRepo;
    public ProductService(ProductRepository prodRepo) {
        this.prodRepo = prodRepo;
    }

    public Product addProduct(Product product){
       return prodRepo.save(product);
    }
}
