package com.example.PlugLess.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PlugLess.dto.friend.FriendRequestResponse;
import com.example.PlugLess.dto.user.UserResponse;
import com.example.PlugLess.services.FriendService;

@RestController
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/request/{targetId}")
    public ResponseEntity<Void> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String targetId) {
        friendService.sendRequest(userDetails.getUsername(), targetId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept/{requesterId}")
    public ResponseEntity<Void> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String requesterId) {
        friendService.acceptRequest(userDetails.getUsername(), requesterId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{requesterId}")
    public ResponseEntity<Void> rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String requesterId) {
        friendService.rejectRequest(userDetails.getUsername(), requesterId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String friendId) {
        friendService.removeFriend(userDetails.getUsername(), friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<UserResponse> getMyFriends(
            @AuthenticationPrincipal UserDetails userDetails) {
        return friendService.getMyFriends(userDetails.getUsername());
    }

    @GetMapping("/requests")
    public List<FriendRequestResponse> getIncomingRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return friendService.getIncomingRequests(userDetails.getUsername());
    }
}

