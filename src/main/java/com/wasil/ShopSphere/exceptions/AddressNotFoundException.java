package com.wasil.ShopSphere.exceptions;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String message){
        super(message);
    }
}
