package com.example.PlugLess.dto.friend;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FriendRequestResponse {
    private String requesterId;
    private String requesterUserName;
    private String requesterDisplayName;
    private String requesterProfileImageUrl;
    private boolean isOnline;
    private Instant lastSeen;
}
