package com.example.PlugLess.dto.auth;

import com.example.PlugLess.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserResponse user;
}
