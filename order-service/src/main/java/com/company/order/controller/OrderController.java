package com.company.order.controller;

import com.company.order.dto.OrderRequest;
import com.company.order.dto.OrderResponse;
import com.company.order.entity.Order;
import com.company.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Get all orders for the authenticated user (JWT required)
     */
    @GetMapping
    public ResponseEntity<?> getUserOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User ID not found in token");
        }

        List<Order> orders = orderRepository.findByUserId(userId);
        List<OrderResponse> responses = orders.stream()
                .map(order -> new OrderResponse(
                        order.getId(),
                        order.getUserId(),
                        order.getOrderDetails(),
                        order.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Create a new order for the authenticated user (JWT required)
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody OrderRequest orderRequest,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User ID not found in token");
        }

        Order order = new Order(userId, orderRequest.getOrderDetails());
        Order savedOrder = orderRepository.save(order);

        OrderResponse response = new OrderResponse(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getOrderDetails(),
                savedOrder.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a specific order by ID (JWT required)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable("id") Long id,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User ID not found in token");
        }

        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order not found");
        }

        Order foundOrder = order.get();

        // Check if the order belongs to the authenticated user
        if (!foundOrder.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to access this order");
        }

        OrderResponse response = new OrderResponse(
                foundOrder.getId(),
                foundOrder.getUserId(),
                foundOrder.getOrderDetails(),
                foundOrder.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}

