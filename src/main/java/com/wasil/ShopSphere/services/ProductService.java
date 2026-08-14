package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.product.ProductRequest;
import com.wasil.ShopSphere.dto.product.ProductResponse;
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

    public ProductResponse addProduct(ProductRequest productRequest) {
       Product product = new Product();
       product.setProdName(productRequest.getProdName());
       product.setProdPrice(productRequest.getProdPrice());
       product.setProdDescription(productRequest.getProdDescription());
       product.setProdStock(productRequest.getProdStock());
       Product savedProduct =  prodRepo.save(product);
       return convertToResponse(savedProduct);
    }

    public List<ProductResponse> getAllProducts(){
        return prodRepo.findAll().stream().map(this::convertToResponse).toList();
    }

    public ProductResponse getProductById(Long id){
        Product product = prodRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id :" + id));
        return convertToResponse(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequest productRequest){
        Product existingProduct = prodRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id :" + id));
        existingProduct.setProdName(productRequest.getProdName());
        existingProduct.setProdPrice(productRequest.getProdPrice());
        existingProduct.setProdDescription(productRequest.getProdDescription());
        existingProduct.setProdStock(productRequest.getProdStock());
        Product updatedProduct = prodRepo.save(existingProduct);
        return convertToResponse(updatedProduct);
    }

    public void deleteProduct(Long id){
        Product existingProduct = prodRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id :" + id));
        prodRepo.delete(existingProduct);
    }

    private ProductResponse convertToResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setProdId(product.getProdId());
        response.setProdName(product.getProdName());
        response.setProdPrice(product.getProdPrice());
        response.setProdDescription(product.getProdDescription());
        response.setProdStock(product.getProdStock());
        response.setProdCreatedAt(product.getProdCreatedAt());
        response.setProdUpdatedAt(product.getProdUpdatedAt());

        return response;
    }
}
