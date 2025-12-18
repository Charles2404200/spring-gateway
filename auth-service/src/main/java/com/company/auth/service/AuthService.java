package com.company.auth.service;

import com.company.auth.dto.LoginRequest;
import com.company.auth.dto.LoginResponse;
import com.company.auth.entity.User;
import com.company.auth.repository.UserRepository;
import com.company.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Service
 *
 * Business logic for:
 * - User login
 * - User registration
 * - Token validation
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Authenticate user with username and password
     *
     * @param loginRequest username and password
     * @return LoginResponse with JWT token or error message
     */
    public LoginResponse authenticate(LoginRequest loginRequest) {
        // Validate input
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Find user by username
        Optional<User> user = userRepository.findByUsername(loginRequest.getUsername());

        if (user.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }

        User foundUser = user.get();

        // Validate password (in production, use BCryptPasswordEncoder)
        if (!foundUser.getPassword().equals(loginRequest.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(foundUser.getId(), foundUser.getUsername());

        // Create and return response
        return new LoginResponse(
                token,
                foundUser.getId(),
                foundUser.getUsername(),
                foundUser.getEmail(),
                jwtExpiration / 1000 // Convert milliseconds to seconds
        );
    }

    /**
     * Register a new user
     *
     * @param registerRequest username and password
     * @return LoginResponse with JWT token for the new user
     */
    public LoginResponse register(LoginRequest registerRequest) {
        // Validate input
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByUsername(registerRequest.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(registerRequest.getPassword()); // In production, use BCryptPasswordEncoder
        newUser.setEmail(registerRequest.getUsername() + "@example.com");

        User savedUser = userRepository.save(newUser);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername());

        // Create and return response
        return new LoginResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                jwtExpiration / 1000 // Convert milliseconds to seconds
        );
    }

    /**
     * Validate JWT token
     *
     * @param token JWT token to validate
     * @return Map containing validation result
     */
    public Map<String, Object> validateToken(String token) {
        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (token == null || token.trim().isEmpty()) {
            response.put("valid", false);
            response.put("error", "Token is required");
            return response;
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            response.put("valid", false);
            response.put("error", "Invalid or expired token");
            return response;
        }

        try {
            // Extract user information from token
            Long userId = jwtUtil.extractUserId(token);
            String username = jwtUtil.extractUsername(token);

            response.put("valid", true);
            response.put("userId", userId);
            response.put("username", username);

            return response;
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", "Failed to extract token claims: " + e.getMessage());
            return response;
        }
    }

    /**
     * Get user by ID
     *
     * @param id user ID
     * @return User if found, throws exception if not found
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

