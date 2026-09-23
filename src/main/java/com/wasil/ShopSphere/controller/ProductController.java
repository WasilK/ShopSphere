package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.product.ProductImageResponse;
import com.wasil.ShopSphere.dto.product.ProductRequest;
import com.wasil.ShopSphere.dto.product.ProductResponse;
import com.wasil.ShopSphere.services.ProductImageService;
import com.wasil.ShopSphere.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Product API's")
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;

    public ProductController(ProductService productService, ProductImageService productImageService) {
        this.productImageService = productImageService;
        this.productService = productService;
    }
    @Operation(summary = "Adds a product, only allowed for admin.")
    @PostMapping
    public ProductResponse addProduct(@Valid @RequestBody ProductRequest productRequest){
        return productService.addProduct(productRequest);
    }
    @Operation(summary = "Gets a product using product id.")
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }

    @Operation(summary = "Updates a product using product id, only allowed for admin.")
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest){
        return productService.updateProduct(id, productRequest);
    }
    @Operation(summary = "Deactivates a product using product id, only allowed for admin.")
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
    }
    @Operation(summary = "Adds product images to a product using product id, only allowed for admin.")
    @PostMapping("/{id}/images")
    public ProductImageResponse uploadProductImage(@PathVariable Long id, @RequestParam("file") MultipartFile file){
       return productImageService.uploadProductImage(id, file);
    }
    @Operation(summary = "Gets all the products.")
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
    @Operation(summary = "Gets images of a product using product id.")
    @GetMapping("/{id}/images")
    public List<ProductImageResponse> getProductImages(@PathVariable Long id){
        return productImageService.getProductImages(id);
    }
    @Operation(summary = "Gets a particular image of a product using product id and image id.")
    @GetMapping("/{id}/images/{imageId}")
    public ProductImageResponse getProductImage(@PathVariable Long id, @PathVariable Long imageId){
        return productImageService.getProductImageById(id, imageId);
    }
    @Operation(summary = "Deletes the image of a product using product id and image id, only allowed for admin.")
    @DeleteMapping("/{id}/images/{imageId}")
    public void deleteProductImage(@PathVariable Long id, @PathVariable Long imageId){
        productImageService.deleteProductImage(id, imageId);
    }
    @Operation(summary = "Used to set a primary product image, only allowed for admin.")
    @PutMapping("/{id}/images/{imageId}")
    public ProductImageResponse setProductImagePrimary(@PathVariable Long id, @PathVariable Long imageId){
        return productImageService.setProductImagePrimary(id, imageId);
    }
}
