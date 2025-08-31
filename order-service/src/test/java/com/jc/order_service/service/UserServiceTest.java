package com.jc.order_service.service;

import com.jc.order_service.client.UserClient;
import com.jc.order_service.exception.ExternalServiceException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @MockitoBean
    private UserClient userClient;

    @Autowired
    private UserService userService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker("userService").reset();
    }

    @Test
    void testGetUserById_Success() {
        UserClient.UserResponse mockUser = new UserClient.UserResponse(1L, "John Doe", "john@example.com");
        when(userClient.getUserById(1L)).thenReturn(mockUser);

        UserClient.UserResponse user = userService.getUser(1L);
        assertNotNull(user);
        assertEquals("John Doe", user.name());
        verify(userClient, times(1)).getUserById(1L);
    }

    @Test
    void testGetUserById_FallbackTriggered() {
        when(userClient.getUserById(1L)).thenThrow(new RuntimeException("Service down"));

        ExternalServiceException ex = assertThrows(ExternalServiceException.class, () -> userService.getUser(1L));
        System.out.println(ex.getMessage());
        assertTrue(ex.getMessage().contains("User service"));
    }

    @Test
    void testRateLimiterExceeded() {
        // Simulate rate limiter trigger
        ExternalServiceException ex = assertThrows(ExternalServiceException.class, () -> {
            for (int i = 0; i < 10; i++) {
                userService.getUser(1L);
            }
        });

        assertNotNull(ex.getMessage());
    }

}
