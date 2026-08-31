package com.wasil.ShopSphere.exceptions;

public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message){
        super(message);
    }
}
