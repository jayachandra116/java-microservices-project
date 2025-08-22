package com.jc.product_service.controller;

import com.jc.product_service.model.Product;
import com.jc.product_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @PostMapping("/{id}/decrement-stock")
    public ResponseEntity<String> decrementStock(@PathVariable Long id, @RequestParam int quantity) {
        Product product = productService.getProductById(id);
        if (product.getStock() < quantity) {
            return ResponseEntity.badRequest().body("Insufficient stock for product ID: " + id);
        }
        product.setStock(product.getStock() - quantity);
        productService.updateProduct(product.getId(), product);
        return ResponseEntity.ok("Stock updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
