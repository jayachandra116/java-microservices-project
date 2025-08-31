package com.jc.order_service.service;

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
    private final UserService userService;
    private final ProductService productService;

    public OrderServiceImpl(OrderRepository orderRepository, UserService userService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
    }


    @Override
    public Order createOrder(Order order) {

        var user = userService.getUser(order.getUserId());
        var product = productService.getProduct(order.getProductId());

        if (user == null) {
            throw new UserNotFoundException(order.getUserId());
        }
        if (product == null) {
            throw new ProductNotFoundException(order.getProductId());
        }

        if (order.getQuantity() > product.stock()) {
            throw new InsufficientStockException(order.getProductId(), order.getQuantity(), product.stock());
        }

        Order savedOrder = orderRepository.save(order);
        productService.decrementStock(product.id(), order.getQuantity());
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
