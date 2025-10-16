package com.valentin.file_manager_server.controller;

import com.valentin.file_manager_server.model.dto.AuthRequest;
import com.valentin.file_manager_server.model.dto.AuthResponse;
import com.valentin.file_manager_server.security.jwt.JwtUtil;
import com.valentin.file_manager_server.service.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AppUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> regularLogin(@RequestBody AuthRequest request) throws AccessDeniedException {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());

            if (userDetailsService.passwordMismatch(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid password");
            }

            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> regularRegister(@RequestBody AuthRequest req) {
        try {
            if (userDetailsService.existsByEmail(req.getEmail())) {
                return ResponseEntity.badRequest().body("Email already registered.");
            }

            userDetailsService.createRegularUser(req.getEmail(), req.getPassword());

            return ResponseEntity.ok("User registered.");
        } catch (Exception e) {
            throw new RuntimeException("Register failed: " + e.getMessage());
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of(
                    "ok", false,
                    "error", "missing_token"
            ));
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of(
                    "ok", false,
                    "error", "invalid_or_expired"
            ));
        }

        String user = jwtUtil.extractUsername(token);
        List<String> roles = jwtUtil.extractRoles(token);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "user", user,
                "roles", roles
        ));
    }
}
