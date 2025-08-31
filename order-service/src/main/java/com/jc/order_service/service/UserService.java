package com.jc.order_service.service;

import com.jc.order_service.client.UserClient;
import com.jc.order_service.exception.ExternalServiceException;
import com.jc.order_service.exception.UserNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.ConnectException;

@Service
public class UserService {

    private final UserClient userClient;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserClient userClient) {
        this.userClient = userClient;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "userServiceFallback")
    @Retry(name = "userServiceRetry", fallbackMethod = "userServiceFallback")
    @RateLimiter(name = "userServiceRL", fallbackMethod = "userServiceFallback")
    public UserClient.UserResponse getUser(Long userId) {
        logger.info("Fetching user details from user service");
        return userClient.getUserById(userId);
    }

    public UserClient.UserResponse userServiceFallback(Long userId, Throwable t) {
        logger.error("Error from user service: {}, Running Fallback method", t.getMessage());
        switch (t) {
            case FeignException.NotFound notFound -> throw new UserNotFoundException(userId);
            case CallNotPermittedException callNotPermittedException ->
                    throw new ExternalServiceException("User service: The circuit breaker is open: " + t.getMessage());
            case RequestNotPermitted requestNotPermitted ->
                    throw new ExternalServiceException("User service: The rate limit has been exceeded: " + t.getMessage());
            case ConnectException connectException ->
                    throw new ExternalServiceException("User service: Cannot connect to user service: " + t.getMessage());
            default ->
                    throw new ExternalServiceException("User service: An unexpected error occurred during call:  " + t.getMessage());
        }
    }

}
