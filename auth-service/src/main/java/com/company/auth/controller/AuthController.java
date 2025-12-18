package com.company.auth.controller;

import com.company.auth.dto.LoginRequest;
import com.company.auth.dto.LoginResponse;
import com.company.auth.dto.ValidateTokenRequest;
import com.company.auth.entity.User;
import com.company.auth.repository.UserRepository;
import com.company.auth.service.AuthService;
import com.company.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Centralized Authentication Controller
 *
 * Handles:
 * - User registration
 * - User login (JWT token generation)
 * - Token validation (for other services)
 * - User information retrieval
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * User Login - Issues JWT Token
     *
     * POST /auth/login
     * {
     *   "username": "john",
     *   "password": "password123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticate(loginRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * User Registration
     *
     * POST /auth/register
     * {
     *   "username": "john",
     *   "password": "password123",
     *   "email": "john@example.com"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest registerRequest) {
        try {
            LoginResponse response = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Validate Token Endpoint - for other services to verify JWT tokens
     *
     * POST /auth/validate-token
     * {
     *   "token": "eyJhbGciOiJIUzUxMiJ9..."
     * }
     *
     * Response on success:
     * {
     *   "valid": true,
     *   "userId": 1,
     *   "username": "john"
     * }
     *
     * Response on failure:
     * {
     *   "valid": false,
     *   "error": "Token expired"
     * }
     */
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody ValidateTokenRequest request) {
        Map<String, Object> response = authService.validateToken(request.getToken());

        // Check if validation was successful
        Boolean isValid = (Boolean) response.get("valid");
        if (isValid == null || !isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get user information by ID
     *
     * GET /auth/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
        return ResponseEntity.ok(user.get());
    }

    /**
     * Get all users
     *
     * GET /auth/users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Health check endpoint
     *
     * GET /auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "auth-service",
                "message", "Auth service is running"
        ));
    }
}

