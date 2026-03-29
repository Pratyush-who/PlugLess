package com.example.PlugLess.dto.friend;

import java.time.Instant;

import lombok.Getter;

@Getter
public class SentFriendRequestResponse {
    private final String targetId;
    private final String targetUserName;
    private final String targetDisplayName;
    private final String targetProfileImageUrl;
    private final boolean isOnline;
    private final Instant lastSeen;

    // Canonical user-like aliases used by some clients.
    private final String id;
    private final String userName;
    private final String displayName;
    private final String profileImageUrl;

    // Nested object used by clients that expect request.recipient.{...}
    private final Recipient recipient;

    public SentFriendRequestResponse(String targetId, String targetUserName, String targetDisplayName,
                                     String targetProfileImageUrl, boolean isOnline, Instant lastSeen) {
        this.targetId = targetId;
        this.targetUserName = targetUserName;
        this.targetDisplayName = targetDisplayName;
        this.targetProfileImageUrl = targetProfileImageUrl;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;

        this.id = targetId;
        this.userName = targetUserName;
        this.displayName = targetDisplayName;
        this.profileImageUrl = targetProfileImageUrl;
        this.recipient = new Recipient(targetId, targetUserName, targetDisplayName, targetProfileImageUrl, isOnline, lastSeen);
    }

    @Getter
    public static class Recipient {
        private final String id;
        private final String userName;
        private final String displayName;
        private final String profileImageUrl;
        private final boolean isOnline;
        private final Instant lastSeen;

        public Recipient(String id, String userName, String displayName, String profileImageUrl,
                         boolean isOnline, Instant lastSeen) {
            this.id = id;
            this.userName = userName;
            this.displayName = displayName;
            this.profileImageUrl = profileImageUrl;
            this.isOnline = isOnline;
            this.lastSeen = lastSeen;
        }
    }
}

