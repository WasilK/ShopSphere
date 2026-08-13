package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository prodRepo;
    public ProductService(ProductRepository prodRepo) {
        this.prodRepo = prodRepo;
    }

    public Product addProduct(Product product){
       return prodRepo.save(product);
    }

    public List<Product> getAllProducts(){
        return prodRepo.findAll();
    }
}
