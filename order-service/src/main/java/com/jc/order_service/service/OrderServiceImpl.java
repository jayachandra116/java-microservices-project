package com.jc.order_service.service;

import com.jc.order_service.exception.OrderNotFoundException;
import com.jc.order_service.model.Order;
import com.jc.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order updateOrder(Long id, Order order) {
        Order existing = getOrderById(id);
        existing.setQuantity(order.getQuantity());
        existing.setStatus(order.getStatus());
        return orderRepository.save(existing);
    }

    @Override
    public void deleteOrder(Long id) {
        Order existing = getOrderById(id);
        orderRepository.delete(existing);

    }
}
