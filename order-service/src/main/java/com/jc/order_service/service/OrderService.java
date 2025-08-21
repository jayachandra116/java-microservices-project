package com.jc.order_service.service;

import com.jc.order_service.model.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(Order order);

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    Order updateOrder(Long id, Order order);

    void deleteOrder(Long id);
}
