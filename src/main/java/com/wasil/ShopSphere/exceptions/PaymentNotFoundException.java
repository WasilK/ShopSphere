package com.wasil.ShopSphere.exceptions;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message){
        super(message);
    }
}
