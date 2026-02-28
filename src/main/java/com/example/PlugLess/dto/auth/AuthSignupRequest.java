package com.example.PlugLess.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

