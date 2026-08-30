package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.exceptions.ProductImageNotFoundException;
import com.wasil.ShopSphere.exceptions.ProductNotFoundException;
import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.model.ProductImage;
import com.wasil.ShopSphere.dto.product.ProductImageResponse;
import com.wasil.ShopSphere.repositories.ProductImageRepository;
import com.wasil.ShopSphere.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class ProductImageService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;

    public ProductImageService(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            CloudinaryService cloudinaryService) {

        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public ProductImageResponse uploadProductImage(Long productId, MultipartFile file) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + productId
                        )
                );

        String imageUrl = cloudinaryService.uploadImage(file);

        ProductImage productImage = new ProductImage();

        productImage.setImageUrl(imageUrl);
        productImage.setPrimaryImage(false);
        productImage.setProduct(product);

        return convertToResponse(productImageRepository.save(productImage));
    }



    private ProductImageResponse convertToResponse(ProductImage productImage){
        ProductImageResponse response = new ProductImageResponse();
        response.setId(productImage.getImageId());
        response.setImageUrl(productImage.getImageUrl());
        response.setPrimaryImage(productImage.getPrimaryImage());
        response.setProdId(productImage.getProduct().getProdId());
        return response;
    }

}
