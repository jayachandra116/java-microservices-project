package com.jc.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @GetMapping("/users/{id}")
    public ResponseEntity<String> getUser(@PathVariable String id) {
        return ResponseEntity.ok("User details for ID: " + id);
    }

    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody String user) {
        return ResponseEntity.ok("User created: " + user);
    }
}
