package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.exceptions.ProductNotFoundException;
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

    public Product getProductById(Long id){
        return prodRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id :" + id));
    }

    public Product updateProduct(Long id, Product product){
        Product existingProduct = prodRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id :" + id));
        existingProduct.setProdName(product.getProdName());
        existingProduct.setProdPrice(product.getProdPrice());
        existingProduct.setProdDescription(product.getProdDescription());
        existingProduct.setProdStock(product.getProdStock());
        return prodRepo.save(existingProduct);
    }

    public void deleteProduct(Long id){
        Product existingProduct = prodRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id :" + id));
        prodRepo.delete(existingProduct);
    }
}
