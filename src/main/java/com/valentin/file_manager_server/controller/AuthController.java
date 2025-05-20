package com.valentin.file_manager_server.controller;

import com.valentin.file_manager_server.model.AuthRequest;
import com.valentin.file_manager_server.model.AuthResponse;
import com.valentin.file_manager_server.security.JwtUtil;
import com.valentin.file_manager_server.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AppUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> regularLogin(@RequestBody AuthRequest request) {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());

            if (userDetailsService.passwordMismatch(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid password");
            }

            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> regularRegister(@RequestBody AuthRequest req) {
        if (userDetailsService.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered.");
        }

        userDetailsService.createRegularUser(req.getEmail(), req.getPassword());

        return ResponseEntity.ok("User registered.");
    }
}
