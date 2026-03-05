package com.example.PlugLess.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.PlugLess.dto.user.UserResponse;
import com.example.PlugLess.entity.User;
import com.example.PlugLess.repository.UserRepository;

@Service
public class PresenceService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public PresenceService(UserRepository userRepository, SimpMessagingTemplate messagingTemplate,
                           UserService userService) {
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    public void markOnline(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setOnline(true);
            userRepository.save(user);
            broadcastPresence(user);
        });
    }

    public void markOffline(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setOnline(false);
            user.setLastSeen(Instant.now());
            userRepository.save(user);
            broadcastPresence(user);
        });
    }

    public List<UserResponse> getOnlineUsers() {
        return userRepository.findAllByIsOnline(true)
                .stream()
                .map(userService::toResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getOnlineStats() {
        long online = userRepository.countByIsOnline(true);
        long total = userRepository.count();
        return Map.of("onlineCount", online, "totalCount", total);
    }

    private void broadcastPresence(User user) {
        PresenceEvent event = new PresenceEvent(
            user.getId(),
            user.getUserName(),
            user.isOnline(),
            user.getLastSeen()
        );
        messagingTemplate.convertAndSend("/topic/presence", event);
    }

    public record PresenceEvent(
        String userId,
        String userName,
        boolean isOnline,
        Instant lastSeen
    ) {}
}


