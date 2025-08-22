package com.jc.order_service.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product with ID: " + productId + " not found");
    }
}
