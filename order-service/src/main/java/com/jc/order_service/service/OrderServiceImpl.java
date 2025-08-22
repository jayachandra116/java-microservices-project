package com.jc.order_service.service;

import com.jc.order_service.client.ProductClient;
import com.jc.order_service.client.UserClient;
import com.jc.order_service.exception.InsufficientStockException;
import com.jc.order_service.exception.OrderNotFoundException;
import com.jc.order_service.exception.ProductNotFoundException;
import com.jc.order_service.exception.UserNotFoundException;
import com.jc.order_service.model.Order;
import com.jc.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;

    public OrderServiceImpl(OrderRepository orderRepository, UserClient userClient, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.productClient = productClient;
    }

    @Override
    public Order createOrder(Order order) {

        try {
            userClient.getUserById(order.getUserId());
        } catch (Exception e) {
            throw new UserNotFoundException(order.getUserId());
        }

        var product = productClient.getProductById(order.getProductId());
        if (product == null) {
            throw new ProductNotFoundException(order.getProductId());
        }

        if (order.getQuantity() > product.stock()) {
            throw new InsufficientStockException(order.getProductId(), order.getQuantity(), product.stock());
        }

        Order savedOrder = orderRepository.save(order);

        try {
            productClient.decrementStock(product.id(), order.getQuantity());
        } catch (Exception e) {
            System.out.println("Failed to decrement stock for productId " + order.getProductId() + ": " + e.getMessage());
        }
        return savedOrder;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
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
