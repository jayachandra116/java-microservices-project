package com.jc.product_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {

    @GetMapping("/products/{id}")
    public ResponseEntity<String> getProduct(@PathVariable String id) {
        return ResponseEntity.ok("Product details for ID: " + id);
    }

    @PostMapping("/products")
    public ResponseEntity<String> createProduct(@RequestBody String product) {
        return ResponseEntity.ok("Product created: " + product);
    }
}
