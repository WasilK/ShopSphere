package com.wasil.ShopSphere.repositories;

import com.wasil.ShopSphere.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByProdNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByProdPriceBetween(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );
}
