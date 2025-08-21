package com.jc.order_service.service;


import com.jc.order_service.entity.OrderStatus;
import com.jc.order_service.exception.OrderNotFoundException;
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

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        order = Order.builder()
                .id(1L)
                .userId(1L)
                .productId(2L)
                .quantity(3)
                .status(OrderStatus.PENDING)
                .build();
    }

    @Test
    void shouldCreateOrder() {
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

}
