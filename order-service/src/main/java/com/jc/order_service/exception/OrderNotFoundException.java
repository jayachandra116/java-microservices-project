package com.jc.order_service.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order with ID: " + id + " not found");
    }
}
