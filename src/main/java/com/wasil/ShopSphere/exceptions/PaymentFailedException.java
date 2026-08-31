package com.wasil.ShopSphere.exceptions;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message){
        super(message);
    }
}
