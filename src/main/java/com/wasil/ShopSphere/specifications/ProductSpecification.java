package com.wasil.ShopSphere.specifications;

import com.wasil.ShopSphere.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> hasName(String name) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                root.get("prodName")
                        ),
                        "%" + name.toLowerCase() + "%"
                );
    }

    public static Specification<Product> hasMaxPrice(BigDecimal maxPrice){
        return (root, query, criteriaBuilder) -> (
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("prodPrice"),
                        maxPrice
                )
                );
    }
    public static Specification<Product> hasMinPrice(BigDecimal minPrice){
        return (root, query, criteriaBuilder) -> (
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("prodPrice"),
                        minPrice
                )
                );
    }
    public static Specification<Product> hasCategory(
            Long categoryId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("category").get("categoryId"),
                        categoryId
                );
    }

    public static Specification<Product> isActive() {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("prodIsActive"));
    }
}
