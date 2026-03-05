package com.example.PlugLess.dto.user;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private String id;
    private String email;
    private String userName;
    private String displayName;
    private String bio;
    private String status;
    private String profileImageUrl;
    private Instant lastSeen;
    private boolean isOnline;
    private List<String> friendIds;
    private List<String> friendRequestIds;
    private Instant createdAt;
}
