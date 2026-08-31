package com.wasil.ShopSphere.exceptions;

public class PaymentAmountMismatchException extends RuntimeException {
    public PaymentAmountMismatchException(String message){
        super(message);
    }
}
