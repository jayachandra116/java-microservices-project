package com.jc.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", url = "${PRODUCT_SERVICE_URL:http://localhost:8082}")
public interface ProductClient {

    record ProductResponse(Long id, String name, Double price, String description, Integer stock) {
    }

    @GetMapping("/products/{id}")
    ProductResponse getProductById(@PathVariable Long id);

    @PostMapping("/products/{id}/decrement-stock")
    void decrementStock(@PathVariable Long id, @RequestParam int quantity);

}
