package com.jc.order_service.service;

import com.jc.order_service.client.ProductClient;
import com.jc.order_service.exception.ExternalServiceException;
import com.jc.order_service.exception.ProductNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.ConnectException;

@Service
public class ProductService {

    private final ProductClient productClient;

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public ProductService(ProductClient productClient) {
        this.productClient = productClient;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "productServiceFallback")
    @Retry(name = "productServiceRetry", fallbackMethod = "productServiceFallback")
    @RateLimiter(name = "productServiceRL", fallbackMethod = "productServiceFallback")
    public ProductClient.ProductResponse getProduct(Long productId) {
        logger.info("Fetching product details from product service");
        return productClient.getProductById(productId);
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "productStockServiceFallback")
    @Retry(name = "productServiceRetry", fallbackMethod = "productStockServiceFallback")
    @RateLimiter(name = "productServiceRL", fallbackMethod = "productStockServiceFallback")
    public void decrementStock(@PathVariable Long id, @RequestParam int quantity) {
        logger.info("Updating stock of the product in product service");
        productClient.decrementStock(id, quantity);
    }

    public ProductClient.ProductResponse productServiceFallback(Long productId, Throwable t) {
        logger.error("Error from product service: {}. Running fallback method", t.getMessage());
        switch (t) {
            case FeignException.NotFound notFound -> throw new ProductNotFoundException(productId);
            case CallNotPermittedException callNotPermittedException ->
                    throw new ExternalServiceException("Product service: The circuit breaker is open: " + t.getMessage());
            case RequestNotPermitted requestNotPermitted ->
                    throw new ExternalServiceException("Product service: The rate limit has been exceeded: " + t.getMessage());
            case ConnectException connectException ->
                    throw new ExternalServiceException("Product service: Cannot connect to user service: " + t.getMessage());
            default ->
                    throw new ExternalServiceException("Product service: An unexpected error occurred during call:  " + t.getMessage());
        }
    }

    public ProductClient.ProductResponse productStockServiceFallback(@PathVariable Long productId, @RequestParam int quantity, Throwable t) {
        logger.error("Error while decrementing stock from product service: {}. Running fallback method", t.getMessage());
        switch (t) {
            case FeignException.NotFound notFound -> throw new ProductNotFoundException(productId);
            case CallNotPermittedException callNotPermittedException ->
                    throw new ExternalServiceException("Product service: The circuit breaker is open: " + t.getMessage());
            case RequestNotPermitted requestNotPermitted ->
                    throw new ExternalServiceException("Product service: The rate limit has been exceeded: " + t.getMessage());
            case ConnectException connectException ->
                    throw new ExternalServiceException("Product service: Cannot connect to user service: " + t.getMessage());
            default ->
                    throw new ExternalServiceException("Product service: An unexpected error occurred during call:  " + t.getMessage());
        }
    }
}
