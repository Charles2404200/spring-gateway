package com.company.user;

import com.company.user.entity.User;
import com.company.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    /**
     * Initialize sample data on application startup
     */
    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            // Check if users already exist
            if (userRepository.count() == 0) {
                userRepository.save(new User("john", "password123", "john@example.com"));
                userRepository.save(new User("jane", "password456", "jane@example.com"));
                userRepository.save(new User("admin", "admin123", "admin@example.com"));
                System.out.println("Sample users initialized!");
            }
        };
    }
}


