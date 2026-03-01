package com.example.PlugLess.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthSignupRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 3, max = 32)
    private String userName;

    @NotBlank
    @Size(min = 2, max = 48)
    private String displayName;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
}
