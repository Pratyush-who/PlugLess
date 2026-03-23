package com.example.PlugLess.dto.user;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String displayName;
    private String bio;
    private String status;
    private Instant lastSeen;
    private String profileImageUrl;
}
