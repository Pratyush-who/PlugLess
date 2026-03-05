package com.example.PlugLess.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.PlugLess.dto.user.UserResponse;
import com.example.PlugLess.dto.user.UserUpdateRequest;
import com.example.PlugLess.services.PresenceService;
import com.example.PlugLess.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserService userService;
    private final PresenceService presenceService;

    public UserController(UserService userService, PresenceService presenceService) {
        this.userService = userService;
        this.presenceService = presenceService;
    }

    // ─── My Profile ─────────────────────────────────────────────────────────────

    @GetMapping("/me")
    public UserResponse getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getMyProfile(userDetails.getUsername());
    }

    @PutMapping("/me")
    public UserResponse updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest update) {
        return userService.updateMyProfile(userDetails.getUsername(), update);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteMyAccount(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/photo")
    public UserResponse uploadPhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("photo") MultipartFile photo) {
        return userService.uploadProfileImage(userDetails.getUsername(), photo);
    }

    // ─── Public Profiles ────────────────────────────────────────────────────────

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll();
    }
    /** GET /users/{id} — view any user's profile by ID */
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable String id) {
        return userService.getById(id);
    }

    // ─── Presence / Online Status ────────────────────────────────────────────

    /** GET /users/online — list all currently online users */
    @GetMapping("/online")
    public List<UserResponse> getOnlineUsers() {
        return presenceService.getOnlineUsers();
    }

    /** GET /users/online/stats — { "onlineCount": 5, "totalCount": 42 } */
    @GetMapping("/online/stats")
    public Map<String, Long> getOnlineStats() {
        return presenceService.getOnlineStats();
    }
}

