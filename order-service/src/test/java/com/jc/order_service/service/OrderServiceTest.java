package com.jc.order_service.service;

import com.jc.order_service.client.ProductClient;
import com.jc.order_service.client.UserClient;
import com.jc.order_service.entity.OrderStatus;
import com.jc.order_service.exception.InsufficientStockException;
import com.jc.order_service.exception.OrderNotFoundException;
import com.jc.order_service.exception.ProductNotFoundException;
import com.jc.order_service.exception.UserNotFoundException;
import com.jc.order_service.model.Order;
import com.jc.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private UserService userService;
    @Mock
    private ProductService productService;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        order = Order.builder().id(1L).userId(1L).productId(2L).quantity(3).status(OrderStatus.PENDING).build();
    }

    @Test
    void testCreateOrder_Success() {
        UserClient.UserResponse user = new UserClient.UserResponse(1L, "John Doe", "john@example.com");
        ProductClient.ProductResponse product = new ProductClient.ProductResponse(1L, "Laptop", 10.0, "laptop", 10);

        when(userService.getUser(1L)).thenReturn(user);
        when(productService.getProduct(1L)).thenReturn(product);

        Order Order = new Order(1L, 1L, 1L, 2, OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());
        Order savedOrder = new Order(1L, 1L, 1L, 2, OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.createOrder(Order);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
    }

    @Test
    void testCreateOrder_UserNotFound() {
        when(userService.getUser(1L)).thenThrow(new UserNotFoundException(1L));
        Order order = new Order(1L, 1L, 1L, 2, OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());
        assertThrows(UserNotFoundException.class, () -> orderService.createOrder(order));
    }

    @Test
    void testCreateOrder_ProductNotFound() {
        UserClient.UserResponse user = new UserClient.UserResponse(1L, "John Doe", "john@example.com");
        when(userService.getUser(1L)).thenReturn(user);
        when(productService.getProduct(1L)).thenThrow(new ProductNotFoundException(1L));

        Order order = new Order(1L, 1L, 1L, 2, OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());

        assertThrows(ProductNotFoundException.class, () -> orderService.createOrder(order));
    }

    @Test
    void testCreateOrder_InsufficientStock() {
        UserClient.UserResponse user = new UserClient.UserResponse(1L, "John Doe", "john@example.com");
        ProductClient.ProductResponse product = new ProductClient.ProductResponse(1L, "Laptop", 100.0, "laptop", 1);

        when(userService.getUser(1L)).thenReturn(user);
        when(productService.getProduct(1L)).thenReturn(product);

        Order Order = new Order(1L, 1L, 1L, 5, OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(Order));
    }

    @Test
    void shouldGetOrderById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Order found = orderService.getOrderById(1L);
        assertEquals(1L, found.getId());
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(99L));
    }

    @Test
    void shouldGetAllOrders() {
        when(orderRepository.findAll()).thenReturn(Collections.singletonList(order));
        assertEquals(1, orderService.getAllOrders().size());
    }

    @Test
    void shouldUpdateOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        Order updated = orderService.updateOrder(1L, order);
        assertEquals(3, updated.getQuantity());
    }

    @Test
    void shouldDeleteOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        orderService.deleteOrder(1L);
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void shouldThrowOrderNotFoundWhenDeletingNonExistingOrder() {
        when(orderRepository.existsById(1L)).thenReturn(false);
        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder(1L));
    }

}
