package com.company.order.filter;

import com.company.order.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Constructor injection
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();

        System.out.println("🔍 JwtAuthenticationFilter: " + method + " " + path);

        // Allow OPTIONS (pre-flight) requests to pass through
        if (HttpMethod.OPTIONS.matches(method)) {
            System.out.println("✅ Allowing OPTIONS request");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("📋 Authorization header: " + (authHeader != null ? "Present" : "Missing"));

        // Validate JWT token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Missing Authorization header
            System.out.println("❌ Missing/Invalid Authorization header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing Authorization header. Required format: Authorization: Bearer <token>\"}");
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("🔑 Token extracted: " + token.substring(0, Math.min(20, token.length())) + "...");

        try {
            // Validate token
            if (!jwtUtil.validateToken(token)) {
                System.out.println("❌ Token validation failed");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                return;
            }

            // Extract user info from token
            Long userId = jwtUtil.extractUserId(token);
            String username = jwtUtil.extractUsername(token);

            System.out.println("✅ Token valid! UserId: " + userId + ", Username: " + username);

            // ✅ Set authentication in Spring Security context
            // This tells Spring Security: "This request is authenticated!"
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Store in request attributes for controller to use
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);

            // Continue to controller
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.out.println("❌ Exception in token validation: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token validation failed: " + e.getMessage() + "\"}");
        }
    }
}





