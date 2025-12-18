package com.company.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Extraction Filter (Shared across all services)
 *
 * This filter intercepts requests and extracts user info from JWT token:
 * 1. Reads Authorization header (Bearer token)
 * 2. Calls auth-service to validate and extract user info
 * 3. Sets userId and username as request attributes
 *
 * BEST PRACTICE:
 * - Centralized in common module
 * - Used by order-service, user-service, and any other backend service
 * - Configuration via application.properties: auth-service.url
 *
 * Usage:
 * 1. Add dependency: common module to pom.xml
 * 2. Ensure RestTemplate bean exists in service
 * 3. Set auth-service.url in application.properties
 * 4. Filter auto-registers via @Component
 *
 * @author Platform Team
 * @version 1.0
 */
@Component
public class JwtTokenExtractionFilter extends OncePerRequestFilter {

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Value("${auth-service.url:http://localhost:8082}")
    private String authServiceUrl;

    private static final String VALIDATE_TOKEN_ENDPOINT = "/auth/validate-token";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Only process requests with Bearer token
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX_LENGTH);
            extractAndSetUserInfo(request, token);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract user info from token by calling auth-service
     *
     * @param request HttpServletRequest to set attributes on
     * @param token JWT token string
     */
    private void extractAndSetUserInfo(HttpServletRequest request, String token) {
        if (restTemplate == null) {
            logger.warn("RestTemplate not available - skipping token validation");
            return;
        }

        try {
            // Prepare request to auth-service
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("token", token);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(requestBody, headers);

            // Call auth-service to validate token
            String validationUrl = authServiceUrl + VALIDATE_TOKEN_ENDPOINT;
            Map<String, Object> validationResult = restTemplate.postForObject(
                    validationUrl,
                    httpEntity,
                    Map.class
            );

            // Process validation result
            if (validationResult != null) {
                processValidationResult(request, validationResult);
            }
        } catch (Exception e) {
            logger.error("Error validating token with auth-service: " + e.getMessage(), e);
            // Continue filter chain - controller will handle missing userId
        }
    }

    /**
     * Process validation result from auth-service
     */
    private void processValidationResult(HttpServletRequest request, Map<String, Object> validationResult) {
        Boolean isValid = (Boolean) validationResult.get("valid");

        if (Boolean.TRUE.equals(isValid)) {
            // Extract user info
            Long userId = extractUserId(validationResult);
            String username = (String) validationResult.get("username");

            if (userId != null && username != null) {
                // Set as request attributes for controllers to use
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);

                logger.debug("Token validated - UserId: " + userId + ", Username: " + username);
            } else {
                logger.warn("Token valid but missing userId or username in response");
            }
        } else {
            String error = (String) validationResult.get("error");
            logger.warn("Token validation failed: " + error);
        }
    }

    /**
     * Extract userId from validation result (handle various number types)
     *
     * @param validationResult Map containing validation response
     * @return Long userId or null if not found
     */
    private Long extractUserId(Map<String, Object> validationResult) {
        Object userIdObj = validationResult.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }
}

