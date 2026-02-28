package com.example.PlugLess.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.PlugLess.dto.user.UserResponse;
import com.example.PlugLess.dto.user.UserUpdateRequest;
import com.example.PlugLess.entity.User;
import com.example.PlugLess.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUserName(user.getUserName());
        response.setDisplayName(user.getDisplayName());
        response.setBio(user.getBio());
        response.setStatus(user.getStatus());
        response.setLastSeen(user.getLastSeen());
        response.setFriendIds(user.getFriendIds());
        response.setFriendRequestIds(user.getFriendRequestIds());
        response.setCreatedAt(user.getCreatedAt());
        return response;
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

        if (update.getDisplayName() != null) {
            existing.setDisplayName(update.getDisplayName());
        }
        if (update.getBio() != null) {
            existing.setBio(update.getBio());
        }
        if (update.getStatus() != null) {
            existing.setStatus(update.getStatus());
        }
        if (update.getLastSeen() != null) {
            existing.setLastSeen(update.getLastSeen());
        }
        if (update.getFriendIds() != null) {
            existing.setFriendIds(update.getFriendIds());
        }
        if (update.getFriendRequestIds() != null) {
            existing.setFriendRequestIds(update.getFriendRequestIds());
        }

        User saved = userRepository.save(existing);
        return toResponse(saved);
    }

    public void delete(String id) {
        User existing = getEntityById(id);
        userRepository.delete(existing);
    }

    private User getEntityById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
