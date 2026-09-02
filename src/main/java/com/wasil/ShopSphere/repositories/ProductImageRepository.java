package com.wasil.ShopSphere.repositories;

import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);

    Optional<ProductImage> findByImageIdAndProduct(
            Long imageId,
            Product product
    );

    boolean existsByProduct(Product product);
}
