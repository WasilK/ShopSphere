package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.inventory.InventoryResponse;
import com.wasil.ShopSphere.dto.product.ProductRequest;
import com.wasil.ShopSphere.dto.product.ProductResponse;
import com.wasil.ShopSphere.exceptions.ProductNotFoundException;
import com.wasil.ShopSphere.model.Inventory;
import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.repositories.InventoryRepository;
import com.wasil.ShopSphere.repositories.ProductRepository;
import com.wasil.ShopSphere.specifications.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository prodRepo;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository prodRepo, InventoryRepository inventoryRepository) {
        this.prodRepo = prodRepo;
        this.inventoryRepository = inventoryRepository;
    }

    public ProductResponse addProduct(ProductRequest productRequest) {
       Product product = new Product();
       product.setProdName(productRequest.getProdName());
       product.setProdPrice(productRequest.getProdPrice());
       product.setProdDescription(productRequest.getProdDescription());
       Product savedProduct =  prodRepo.save(product);
       Inventory inventory = new Inventory();
       inventory.setProduct(savedProduct);
       inventory.setCurrentStock(0);
       inventoryRepository.save(inventory);
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
        Product updatedProduct = prodRepo.save(existingProduct);
        return convertToResponse(updatedProduct);
    }

    public void deleteProduct(Long id){
        Product existingProduct = prodRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id :" + id));
        prodRepo.delete(existingProduct);
    }

    public Page<ProductResponse> getProducts(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = prodRepo.findAll(pageable);
        return (productPage.map(this::convertToResponse));
    }

    public Page<ProductResponse> searchAndFilterProducts(
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        Specification<Product> specification =
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (name != null && !name.isBlank()) {
            specification = specification.and(
                    ProductSpecification.hasName(name)
            );
        }

        if (minPrice != null) {
            specification = specification.and(
                    ProductSpecification.hasMinPrice(minPrice)
            );
        }

        if (maxPrice != null) {
            specification = specification.and(
                    ProductSpecification.hasMaxPrice(maxPrice)
            );
        }

        Page<Product> productPage = prodRepo.findAll(
                specification,
                pageable
        );
        return productPage.map(this::convertToResponse);
    }
    private ProductResponse convertToResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setProdId(product.getProdId());
        response.setProdName(product.getProdName());
        response.setProdPrice(product.getProdPrice());
        response.setProdDescription(product.getProdDescription());
        response.setProdCreatedAt(product.getProdCreatedAt());
        response.setProdUpdatedAt(product.getProdUpdatedAt());

        return response;
    }

}
