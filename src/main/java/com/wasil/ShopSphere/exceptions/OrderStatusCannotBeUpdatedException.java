package com.wasil.ShopSphere.exceptions;

public class OrderStatusCannotBeUpdatedException extends RuntimeException {
    public OrderStatusCannotBeUpdatedException(String message) {
        super(message);
    }
}
