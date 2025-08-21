package com.jc.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jc.order_service.entity.OrderStatus;
import com.jc.order_service.model.Order;
import com.jc.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrder() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .userId(1L)
                .productId(2L)
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
                .thenReturn(Arrays.asList(order));

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
