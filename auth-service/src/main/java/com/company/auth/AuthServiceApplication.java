package com.company.auth;

import com.company.auth.entity.User;
import com.company.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            // Check if users already exist
            if (userRepository.count() == 0) {
                // Create user 1: john
                User user1 = new User();
                user1.setUsername("john");
                user1.setPassword("password123");
                user1.setEmail("john@example.com");
                user1.setActive(true);
                userRepository.save(user1);

                // Create user 2: jane
                User user2 = new User();
                user2.setUsername("jane");
                user2.setPassword("password456");
                user2.setEmail("jane@example.com");
                user2.setActive(true);
                userRepository.save(user2);

                // Create user 3: admin
                User user3 = new User();
                user3.setUsername("admin");
                user3.setPassword("admin123");
                user3.setEmail("admin@example.com");
                user3.setActive(true);
                userRepository.save(user3);

                System.out.println("✅ Sample users initialized!");
            }
        };
    }

}

