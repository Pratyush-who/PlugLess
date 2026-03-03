package com.example.PlugLess.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.PlugLess.dto.auth.AuthLoginRequest;
import com.example.PlugLess.dto.auth.AuthResponse;
import com.example.PlugLess.dto.auth.AuthSignupRequest;
import com.example.PlugLess.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // form-data (with optional file upload)
    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    public ResponseEntity<AuthResponse> signupFormData(
            @Valid @ModelAttribute AuthSignupRequest request,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request, photo));
    }

    // raw JSON (photo not supported, use /users/me/photo after login)
    @PostMapping(value = "/signup", consumes = "application/json")
    public ResponseEntity<AuthResponse> signupJson(@Valid @RequestBody AuthSignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request, null));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }
}
