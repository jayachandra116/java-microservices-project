package com.jc.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${USER_SERVICE_URL:http://localhost:8081}")
public interface UserClient {

    record UserResponse(Long id, String name, String email) {
    }

    @GetMapping("/users/{id}")
    UserResponse getUserById(@PathVariable Long id);

}
