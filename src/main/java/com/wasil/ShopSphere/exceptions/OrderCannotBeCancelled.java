package com.wasil.ShopSphere.exceptions;

public class OrderCannotBeCancelled extends RuntimeException {
    public OrderCannotBeCancelled(String message) {
        super(message);
    }
}
