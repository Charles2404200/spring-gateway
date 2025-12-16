package com.company.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @GetMapping
    public Map<String, Object> getAllOrders() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order Service - All Orders");
        response.put("data", new String[]{"order1", "order2", "order3"});
        return response;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Order Service");
    }
}

