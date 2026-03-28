package com.example.PlugLess.dto.user;

import com.example.PlugLess.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicProfileResponse {
    private String id;
    private String userName;
    private String displayName;
    private String bio;
    private String status;
    private String profileImageUrl;
    private Integer friendCount;
    private FriendshipStatus friendshipStatus;
    private List<RelationshipAction> allowedActions;
    private Boolean isOnline;
    private Instant lastSeen;

    public static PublicProfileResponse from(User user, FriendshipStatus status) {
        PublicProfileResponse res = new PublicProfileResponse();
        res.setId(user.getId());
        res.setUserName(user.getUserName());
        res.setDisplayName(user.getDisplayName());
        res.setBio(user.getBio());
        res.setStatus(user.getStatus());
        res.setProfileImageUrl(user.getProfileImageUrl());
        res.setFriendCount(user.getFriendIds() == null ? 0 : user.getFriendIds().size());
        res.setFriendshipStatus(status);
        res.setOnline(user.isOnline());
        res.setLastSeen(user.getLastSeen());
        return res;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Integer getFriendCount() {
        return friendCount;
    }

    public void setFriendCount(Integer friendCount) {
        this.friendCount = friendCount;
    }

    public FriendshipStatus getFriendshipStatus() {
        return friendshipStatus;
    }

    public void setFriendshipStatus(FriendshipStatus friendshipStatus) {
        this.friendshipStatus = friendshipStatus;
    }

    public List<RelationshipAction> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(List<RelationshipAction> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }
}
