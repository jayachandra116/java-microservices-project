package com.jc.order_service.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, int requested, int available) {
        super("Insufficient stock for product ID: " + productId + ". Requested: " + requested + ", Available: " + available);
    }
}
