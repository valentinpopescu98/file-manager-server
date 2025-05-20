package com.valentin.file_manager_server.controller;

import com.valentin.file_manager_server.service.PublisherEmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PublisherEmailVerificationController {

    private final PublisherEmailVerificationService verificationService;

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
        try {
            verificationService.verifyEmail(email);
            return ResponseEntity.ok("Email verification sent. Please check your inbox.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send verification email: " + e.getMessage());
        }
    }
}
