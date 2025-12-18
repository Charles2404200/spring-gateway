package com.company.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Auth Service
 *
 * ⚠️ IMPORTANT: This service is PUBLIC
 * - Login endpoint is OPEN: POST /auth/login
 * - Accessible directly from: Client → Auth-Service (8082)
 * - Also accessible via: Client → Gateway (8080) → Auth-Service (8082)
 *
 * Access patterns:
 * ✅ ALLOWED: Client → Auth-Service (8082) - Direct login
 * ✅ ALLOWED: Client → Gateway (8080) → Auth-Service (8082) - Via gateway
 * ✅ ALLOWED: Other services call /auth/validate-token
 */
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

                // Stateless session management - no session cookies
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules - Auth Service endpoints are PUBLIC
                .authorizeHttpRequests(authz -> authz
                        // Allow CORS pre-flight requests (OPTIONS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Public endpoints - no authentication required
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/validate-token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/users/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/health").permitAll()

                        // Catch-all: deny all other requests
                        .anyRequest().denyAll()
                )

                // Exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                "{\"error\":\"Unauthorized: " + authException.getMessage() + "\"}"
                            );
                        })
                );

        return http.build();
    }
}

