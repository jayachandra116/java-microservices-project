package com.jc.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jc.order_service.client.ProductClient;
import com.jc.order_service.entity.OrderStatus;
import com.jc.order_service.exception.ExternalServiceException;
import com.jc.order_service.exception.InsufficientStockException;
import com.jc.order_service.exception.OrderNotFoundException;
import com.jc.order_service.model.Order;
import com.jc.order_service.service.OrderService;
import com.jc.order_service.service.ProductService;
import com.jc.order_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrder() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .userId(2L)
                .productId(3L)
                .quantity(3)
                .status(OrderStatus.PENDING)
                .build();
        when(orderService.createOrder(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        when(orderService.getOrderById(1L)).thenThrow(new OrderNotFoundException(1L));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found"));
    }

    @Test
    void shouldReturn400WhenInsufficientStock() throws Exception {
        when(orderService.createOrder(Mockito.any(Order.class)))
                .thenThrow(new InsufficientStockException(10L, 5, 2));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"productId\":10,\"quantity\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient stock"));
    }

    @Test
    void shouldDecrementStockWhenOrderIsCreated() throws Exception {
        Order order = new Order(1L, 1L, 10L, 2, OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());

        when(orderService.createOrder(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"productId\":10,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void shouldGetOrderById() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .userId(1L)
                .productId(2L)
                .quantity(3)
                .status(OrderStatus.PENDING)
                .build();
        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldGetAllOrders() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .userId(1L)
                .productId(2L)
                .quantity(3)
                .status(OrderStatus.PENDING)
                .build();
        when(orderService.getAllOrders())
                .thenReturn(Collections.singletonList(order));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .userId(1L)
                .productId(2L)
                .quantity(3)
                .status(OrderStatus.PENDING)
                .build();
        when(orderService.updateOrder(Mockito.eq(1L), any(Order.class))).thenReturn(order);

        mockMvc.perform(put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isNoContent());
    }

}
