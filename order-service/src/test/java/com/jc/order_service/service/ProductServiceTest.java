package com.jc.order_service.service;

import com.jc.order_service.client.ProductClient;
import com.jc.order_service.exception.ExternalServiceException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ProductServiceTest {

    @MockitoBean
    private ProductClient productClient;

    @Autowired
    private ProductService productService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker("productService").reset();
    }

    @Test
    void testGetProductById_FallbackDirectly() {
        when(productClient.getProductById(1L)).thenThrow(new ExternalServiceException("Service down"));
        ExternalServiceException ex = assertThrows(ExternalServiceException.class, () -> productService.getProduct(1L));
//        System.out.println(ex.getMessage());
        assertTrue(ex.getMessage().contains("Product service"));
    }

    @Test
    void testRateLimiterExceeded() {
        ExternalServiceException ex = assertThrows(ExternalServiceException.class, () -> {
            for (int i = 0; i < 10; i++) {
                productService.getProduct(1L);
            }
        });

        assertTrue(ex.getMessage().contains("The rate limit has been exceeded"));
        assertNotNull(ex.getMessage());
    }
}
