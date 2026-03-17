package com.example.PlugLess.services;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.PlugLess.dto.chat.GlobalMessageResponse;
import com.example.PlugLess.entity.GlobalMessage;
import com.example.PlugLess.entity.User;
import com.example.PlugLess.repository.GlobalMessageRepository;
import com.example.PlugLess.repository.UserRepository;

@Service
public class GlobalChatService {

    private final GlobalMessageRepository globalMessageRepository;
    private final UserRepository userRepository;

    public GlobalChatService(GlobalMessageRepository globalMessageRepository,
                             UserRepository userRepository) {
        this.globalMessageRepository = globalMessageRepository;
        this.userRepository = userRepository;
    }

    public GlobalMessageResponse saveMessage(String senderEmail, String content) {
        User sender = userRepository.findByEmail(senderEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        GlobalMessage msg = new GlobalMessage();
        msg.setSenderId(sender.getId());
        msg.setSenderUserName(sender.getUserName());
        msg.setSenderDisplayName(sender.getDisplayName());
        msg.setSenderAvatar(sender.getProfileImageUrl());
        msg.setContent(content.trim());
        msg.setTimestamp(Instant.now());

        GlobalMessage saved = globalMessageRepository.save(msg);
        return toResponse(saved);
    }

    /**
     * Single history method — handles both initial load and lazy load
     *
     * before = null  → initial load, returns latest 50 messages
     * before = timestamp → lazy load, returns 50 messages older than that timestamp
     *
     * Always returns oldest-first (natural chat display order)
     * Flutter knows it hit the beginning when response size < 50
     */
    public List<GlobalMessageResponse> getHistory(Instant before) {
        List<GlobalMessage> messages;

        if (before == null) {
            // initial load — latest 50
            messages = globalMessageRepository.findTop50ByOrderByTimestampDesc();
        } else {
            // scroll-up lazy load — 50 messages before the given timestamp
            messages = globalMessageRepository
                    .findByTimestampBeforeOrderByTimestampDesc(before, PageRequest.of(0, 50));
        }

        Collections.reverse(messages); // oldest-first for chat display
        return messages.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    private GlobalMessageResponse toResponse(GlobalMessage msg) {
        return new GlobalMessageResponse(
            msg.getId(),
            msg.getSenderId(),
            msg.getSenderUserName(),
            msg.getSenderDisplayName(),
            msg.getSenderAvatar(),
            msg.getContent(),
            msg.getTimestamp()
        );
    }
}

