package com.example.PlugLess.dto.user;

import java.time.Instant;
import java.util.List;
public class UserUpdateRequest {
    private String displayName;
    private String bio;
    private String status;
    private Instant lastSeen;
    private List<String> friendIds;
    private List<String> friendRequestIds;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public List<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }

    public List<String> getFriendRequestIds() {
        return friendRequestIds;
    }

    public void setFriendRequestIds(List<String> friendRequestIds) {
        this.friendRequestIds = friendRequestIds;
    }
}

