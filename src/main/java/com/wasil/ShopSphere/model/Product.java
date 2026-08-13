package com.wasil.ShopSphere.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.IdGeneratorType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prodId;
    private String prodName;
    private Double prodPrice;
    private String prodDescription;
    private Integer prodStock;
    @CreationTimestamp
    private Instant prodCreatedAt;
    @UpdateTimestamp
    private Instant prodUpdatedAt;

    public Product(){

    }

    public Product(String prodName, Double prodPrice, String prodDescription) {
        this.prodName = prodName;
        this.prodPrice = prodPrice;
        this.prodDescription = prodDescription;
    }

    public Long getProdId() {
        return prodId;
    }

    public Instant getProdCreatedAt() {
        return prodCreatedAt;
    }

    public Instant getProdUpdatedAt() {
        return prodUpdatedAt;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public Double getProdPrice() {
        return prodPrice;
    }

    public void setProdPrice(Double prodPrice) {
        this.prodPrice = prodPrice;
    }

    public String getProdDescription() {
        return prodDescription;
    }

    public void setProdDescription(String prodDescription) {
        this.prodDescription = prodDescription;
    }

    public Integer getProdStock() {
        return prodStock;
    }

    public void setProdStock(Integer prodStock) {
        this.prodStock = prodStock;
    }
}
