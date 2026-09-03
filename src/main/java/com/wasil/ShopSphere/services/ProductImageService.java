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

import java.util.List;


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
        List<ProductImage> productImages = productImageRepository.findByProduct(product);

        if (productImages.size() >= 10) {
            throw new IllegalStateException(
                    "A product cannot have more than 10 images"
            );
        }

        String imageUrl = cloudinaryService.uploadImage(file);

        ProductImage productImage = new ProductImage();



        productImage.setImageUrl(imageUrl);
        productImage.setPrimaryImage(productImages.isEmpty());
        productImage.setProduct(product);



        return convertToResponse(productImageRepository.save(productImage));
    }

    public List<ProductImageResponse> getProductImages(Long productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + productId
                        )
                );
        return productImageRepository.findByProduct(product).stream()
                .map(this::convertToResponse)
                .toList();
    }

    public ProductImageResponse getProductImageById(Long productId, Long productImageId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + productId
                        )
                );
        ProductImage productImage = productImageRepository.findByImageIdAndProduct(
                productImageId,
                product
        ).orElseThrow(() ->
                new ProductImageNotFoundException(
                        "Product image not found with id: " + productImageId + " for product with id: " + productId
                )
        );
        return convertToResponse(productImage);
    }

    @Transactional
    public void deleteProductImage(Long productId, Long productImageId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + productId
                        )
                );
        ProductImage productImage = productImageRepository.findByImageIdAndProduct(
                productImageId,
                product
        ).orElseThrow(() ->
                new ProductImageNotFoundException(
                        "Product image not found with id: " + productImageId + " for product with id: " + productId
                )
        );
        // 4. Delete image from Cloudinary
        cloudinaryService.deleteImage(productImage.getImageUrl());

        // 5. Delete image from database
        productImageRepository.delete(productImage);
    }

    @Transactional
    public ProductImageResponse setProductImagePrimary(Long productId, Long productImageId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + productId
                        )
                );
        ProductImage productImage = productImageRepository.findByImageIdAndProduct(
                productImageId,
                product
        ).orElseThrow(() ->
                new ProductImageNotFoundException(
                        "Product image not found with id: " + productImageId + " for product with id: " + productId
                )
        );
        List<ProductImage> productImages = productImageRepository.findByProduct(product);
        for(ProductImage prodImage : productImages){
            if(prodImage.getPrimaryImage() && !prodImage.getImageId().equals(productImageId)){
                prodImage.setPrimaryImage(false);
            }
        }
        productImage.setPrimaryImage(true);
        productImageRepository.save(productImage);
        return convertToResponse(productImage);
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
