package com.wasil.ShopSphere.exceptions;

public class PaymentAlreadyProcessedException extends RuntimeException{
    public PaymentAlreadyProcessedException(String message){
        super(message);
    }
}
