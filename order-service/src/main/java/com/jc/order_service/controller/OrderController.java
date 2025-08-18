package com.jc.order_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    @GetMapping("/orders/{id}")
    public ResponseEntity<String> getOrder(@PathVariable String id) {
        return ResponseEntity.ok("Order details for ID: " + id);
    }

    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@RequestBody String order) {
        return ResponseEntity.ok("Order created: " + order);
    }
}
