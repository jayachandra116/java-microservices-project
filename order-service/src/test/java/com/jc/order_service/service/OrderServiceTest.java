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

import java.util.Arrays;
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
    void shouldThrowUserNotFoundWhenUserDoesNotExist() {
        Order order = new Order();
        order.setUserId(1L);
        order.setProductId(10L);
        doThrow(new RuntimeException("404")).when(userClient).getUserById(1L);
        assertThrows(UserNotFoundException.class, () -> orderService.createOrder(order));
    }

    @Test
    void shouldThrowProductNotFoundWhenProductDoesNotExist() {
        Order order = new Order();
        order.setUserId(1L);
        order.setProductId(990L);
        order.setQuantity(5);
        UserClient.UserResponse user = new UserClient.UserResponse(1L, "Joe", "joe@ex.com");
        // User exists
        when(userClient.getUserById(1L)).thenReturn(user);
        // Product fails
        doThrow(new RuntimeException("404")).when(productClient).getProductById(10L);
        assertThrows(ProductNotFoundException.class, () -> orderService.createOrder(order));
    }

    @Test
    void shouldThrowOrderNotFoundWhenOrderMissing() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(1L));
    }

    @Test
    void shouldThrowInsufficientStockWhenQuantityExceedsAvailable() {
        Order order = new Order();
        order.setUserId(1L);
        order.setProductId(10L);
        order.setQuantity(5);
        UserClient.UserResponse user = new UserClient.UserResponse(1L, "Joe", "joe@ex.com");
        when(userClient.getUserById(1L)).thenReturn(user);
        // Mock product with only 2 in stock
        var product = new ProductClient.ProductResponse(10L, "Laptop", 2000.0, "Laptop", 2);
        when(productClient.getProductById(10L)).thenReturn(product);
        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(order));
    }

    @Test
    void shouldCreateOrderWhenUserAndProductExists() {
        Order order = new Order();
        order.setUserId(1L);
        order.setProductId(2L);
        order.setQuantity(1);
        when(userClient.getUserById(1L)).thenReturn(new UserClient.UserResponse(1L, "John", "John@email.com"));
        when(productClient.getProductById(2L)).thenReturn(new ProductClient.ProductResponse(2L, "Laptop", 1200.0, "laptop", 4));
        when(orderRepository.save(order)).thenReturn(order);
        Order created = orderService.createOrder(order);
        assertNotNull(created);
        assertEquals(order, created);
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
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));
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
