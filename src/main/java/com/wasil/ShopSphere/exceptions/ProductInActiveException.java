package com.wasil.ShopSphere.exceptions;

public class ProductInActiveException extends RuntimeException {
    public ProductInActiveException(String message){
        super(message);
    }
}
