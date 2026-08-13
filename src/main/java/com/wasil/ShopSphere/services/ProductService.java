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

    public Product updateProduct(Long id, Product product){
        Product existingProduct = prodRepo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        existingProduct.setProdName(product.getProdName());
        existingProduct.setProdPrice(product.getProdPrice());
        existingProduct.setProdDescription(product.getProdDescription());
        existingProduct.setProdStock(product.getProdStock());
        return prodRepo.save(existingProduct);
    }

    public void deleteProduct(Long id){
        Product existingProduct = prodRepo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        prodRepo.delete(existingProduct);
    }
}
