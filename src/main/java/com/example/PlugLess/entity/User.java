package com.example.PlugLess.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document("users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    private String id;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @NotBlank
    @Indexed(unique = true)
    private String userName;

    @NotBlank
    private String displayName;

    @JsonIgnore
    private String passwordHash;

    private String bio;
    private String status;
    private String profileImageUrl;
    private Instant lastSeen;

    private List<String> friendIds = new ArrayList<>();
    private List<String> friendRequestIds = new ArrayList<>();

    private Instant createdAt = Instant.now();

    public User(String email, String userName, String displayName) {
        this.email = email;
        this.userName = userName;
        this.displayName = displayName;
    }
}
