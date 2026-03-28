package com.example.PlugLess.dto.friend;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SentFriendRequestResponse {
    private String targetId;
    private String targetUserName;
    private String targetDisplayName;
    private String targetProfileImageUrl;
    private boolean isOnline;
    private Instant lastSeen;
}

