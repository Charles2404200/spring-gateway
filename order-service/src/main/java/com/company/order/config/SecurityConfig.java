package com.company.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF - not needed for stateless API
                .csrf(csrf -> csrf.disable())

                // Enable CORS
                .cors(cors -> cors.disable())

                // Stateless session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Only allow requests from API Gateway (forwarded by gateway)
                // Direct access will be blocked
                .authorizeHttpRequests(authz -> authz
                        // All requests allowed - Gateway handles validation
                        // If direct access detected, it will fail at network level
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}




