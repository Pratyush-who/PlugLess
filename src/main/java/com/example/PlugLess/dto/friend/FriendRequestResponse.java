package com.example.PlugLess.dto.friend;

import java.time.Instant;

import lombok.Getter;

@Getter
public class FriendRequestResponse {
    private final String requesterId;
    private final String requesterUserName;
    private final String requesterDisplayName;
    private final String requesterProfileImageUrl;
    private final boolean isOnline;
    private final Instant lastSeen;

    // Canonical user-like aliases used by some clients.
    private final String id;
    private final String userName;
    private final String displayName;
    private final String profileImageUrl;

    // Nested object used by clients that expect request.requester.{...}
    private final Requester requester;

    public FriendRequestResponse(String requesterId, String requesterUserName, String requesterDisplayName,
                                 String requesterProfileImageUrl, boolean isOnline, Instant lastSeen) {
        this.requesterId = requesterId;
        this.requesterUserName = requesterUserName;
        this.requesterDisplayName = requesterDisplayName;
        this.requesterProfileImageUrl = requesterProfileImageUrl;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;

        this.id = requesterId;
        this.userName = requesterUserName;
        this.displayName = requesterDisplayName;
        this.profileImageUrl = requesterProfileImageUrl;
        this.requester = new Requester(requesterId, requesterUserName, requesterDisplayName,
                requesterProfileImageUrl, isOnline, lastSeen);
    }

    @Getter
    public static class Requester {
        private final String id;
        private final String userName;
        private final String displayName;
        private final String profileImageUrl;
        private final boolean isOnline;
        private final Instant lastSeen;

        public Requester(String id, String userName, String displayName, String profileImageUrl,
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
