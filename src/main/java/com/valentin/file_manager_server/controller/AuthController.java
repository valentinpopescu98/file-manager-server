package com.valentin.file_manager_server.controller;

import com.valentin.file_manager_server.model.AuthRequest;
import com.valentin.file_manager_server.model.AuthResponse;
import com.valentin.file_manager_server.security.jwt.JwtUtil;
import com.valentin.file_manager_server.service.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
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
}
