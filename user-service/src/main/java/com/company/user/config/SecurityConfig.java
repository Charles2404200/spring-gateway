package com.company.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for User Service
 *
 * Public endpoints (no authentication required):
 * - POST /users/login - User login
 * - GET /users - List all users
 * - GET /users/{id} - Get specific user
 * - OPTIONS /* - CORS pre-flight requests
 *
 * This service is an authentication provider, so it doesn't enforce
 * JWT validation on its own endpoints. Other services will validate
 * tokens issued by this service.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF - not needed for stateless API
                .csrf().disable()

                // Enable CORS support
                .cors()
                .and()

                // Stateless session management - no session cookies
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // Authorization rules - User Service is public (no auth required)
                .authorizeHttpRequests(authz -> authz
                        // Allow CORS pre-flight requests (OPTIONS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Public endpoints - no authentication required
                        .requestMatchers(HttpMethod.POST, "/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/**").permitAll()

                        // Deny all other requests
                        .anyRequest().denyAll()
                )

                // Exception handling for authentication/authorization errors
                .exceptionHandling()
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write(
                            "{\"error\":\"Unauthorized: " + authException.getMessage() + "\"}"
                        );
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType("application/json");
                        response.getWriter().write(
                            "{\"error\":\"Forbidden: " + accessDeniedException.getMessage() + "\"}"
                        );
                    });

        return http.build();
    }
}

