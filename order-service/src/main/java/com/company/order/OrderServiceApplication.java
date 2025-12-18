package com.company.order;

import com.company.order.entity.Order;
import com.company.order.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    /**
     * RestTemplate bean for calling auth-service
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Initialize sample data on application startup
     */
    @Bean
    public CommandLineRunner initData(OrderRepository orderRepository) {
        return args -> {
            // Check if orders already exist
            if (orderRepository.count() == 0) {
                orderRepository.save(new Order(1L, "Order 001: Laptop + Mouse"));
                orderRepository.save(new Order(1L, "Order 002: Keyboard"));
                orderRepository.save(new Order(2L, "Order 003: Monitor"));
                orderRepository.save(new Order(2L, "Order 004: USB Cable"));
                orderRepository.save(new Order(3L, "Order 005: Headphones"));
                System.out.println("Sample orders initialized!");
            }
        };
    }
}


