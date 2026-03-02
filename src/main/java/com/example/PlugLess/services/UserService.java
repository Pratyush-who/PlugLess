package com.example.PlugLess.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.PlugLess.dto.user.PublicProfileResponse;
import com.example.PlugLess.dto.user.UserResponse;
import com.example.PlugLess.dto.user.UserUpdateRequest;
import com.example.PlugLess.entity.User;
import com.example.PlugLess.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public UserService(UserRepository userRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUserName(user.getUserName());
        response.setDisplayName(user.getDisplayName());
        response.setBio(user.getBio());
        response.setStatus(user.getStatus());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setLastSeen(user.getLastSeen());
        response.setFriendIds(user.getFriendIds());
        response.setFriendRequestIds(user.getFriendRequestIds());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public PublicProfileResponse toPublicProfile(User user) {
        PublicProfileResponse response = new PublicProfileResponse();
        response.setId(user.getId());
        response.setUserName(user.getUserName());
        response.setDisplayName(user.getDisplayName());
        response.setBio(user.getBio());
        response.setStatus(user.getStatus());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setFriendCount(user.getFriendIds() != null ? user.getFriendIds().size() : 0);
        return response;
    }

    // Get own full profile by email (from JWT)
    public UserResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    // Get public profile by username
    public PublicProfileResponse getPublicProfile(String userName) {
        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toPublicProfile(user);
    }

    // Upload profile image for currently logged-in user
    public UserResponse uploadProfileImage(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        String imageUrl = cloudinaryService.uploadProfileImage(file, user.getId());
        user.setProfileImageUrl(imageUrl);
        return toResponse(userRepository.save(user));
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public UserResponse getById(String id) {
        return toResponse(getEntityById(id));
    }

    public UserResponse update(String id, UserUpdateRequest update) {
        User existing = getEntityById(id);

        if (update.getDisplayName() != null) existing.setDisplayName(update.getDisplayName());
        if (update.getBio() != null) existing.setBio(update.getBio());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        if (update.getLastSeen() != null) existing.setLastSeen(update.getLastSeen());
        if (update.getFriendIds() != null) existing.setFriendIds(update.getFriendIds());
        if (update.getFriendRequestIds() != null) existing.setFriendRequestIds(update.getFriendRequestIds());

        return toResponse(userRepository.save(existing));
    }

    public UserResponse updateByEmail(String email, UserUpdateRequest update) {
        User existing = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (update.getDisplayName() != null) existing.setDisplayName(update.getDisplayName());
        if (update.getBio() != null) existing.setBio(update.getBio());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        if (update.getLastSeen() != null) existing.setLastSeen(update.getLastSeen());
        if (update.getFriendIds() != null) existing.setFriendIds(update.getFriendIds());
        if (update.getFriendRequestIds() != null) existing.setFriendRequestIds(update.getFriendRequestIds());

        return toResponse(userRepository.save(existing));
    }

    public void delete(String id) {
        userRepository.delete(getEntityById(id));
    }

    public void deleteByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        userRepository.delete(user);
    }

    private User getEntityById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
