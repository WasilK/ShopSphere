package com.wasil.ShopSphere.exceptions;

import org.springframework.web.bind.annotation.RestControllerAdvice;


public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
