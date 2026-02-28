package com.example.PlugLess.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.PlugLess.dto.auth.AuthLoginRequest;
import com.example.PlugLess.dto.auth.AuthResponse;
import com.example.PlugLess.dto.auth.AuthSignupRequest;
import com.example.PlugLess.dto.user.UserResponse;
import com.example.PlugLess.entity.User;
import com.example.PlugLess.repository.UserRepository;
import com.example.PlugLess.security.JwtService;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public AuthResponse signup(AuthSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use");
        }

        User user = new User(request.getEmail(), request.getUserName(), request.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getEmail());
        UserResponse response = userService.toResponse(saved);
        return new AuthResponse(token, response);
    }

    public AuthResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());
        UserResponse response = userService.toResponse(user);
        return new AuthResponse(token, response);
    }
}

