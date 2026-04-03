package com.example.PlugLess.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.PlugLess.dto.chat.dm.DirectMessageResponse;
import com.example.PlugLess.entity.DirectMessage;
import com.example.PlugLess.entity.User;
import com.example.PlugLess.repository.DirectMessageRepository;
import com.example.PlugLess.repository.UserRepository;

@Service
public class DirectMessageService {

    private static final int DEFAULT_HISTORY_SIZE = 50;
    private static final int MAX_HISTORY_SIZE = 100;

    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DirectMessageService(DirectMessageRepository directMessageRepository,
                                UserRepository userRepository,
                                SimpMessagingTemplate messagingTemplate) {
        this.directMessageRepository = directMessageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public DirectMessageResponse sendMessage(String senderEmail, String recipientId, String content) {
        User sender = getUserByEmail(senderEmail);
        User recipient = getUserById(recipientId);

        if (sender.getId().equals(recipient.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send a DM to yourself");
        }

        if (!areFriends(sender, recipient)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only DM friends");
        }

        String cleanedContent = content == null ? "" : content.trim();
        if (cleanedContent.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty");
        }

        DirectMessage message = new DirectMessage();
        message.setConversationKey(conversationKey(sender.getId(), recipient.getId()));
        message.setSenderId(sender.getId());
        message.setSenderEmail(sender.getEmail());
        message.setSenderUserName(sender.getUserName());
        message.setSenderDisplayName(sender.getDisplayName());
        message.setSenderProfileImageUrl(sender.getProfileImageUrl());
        message.setRecipientId(recipient.getId());
        message.setRecipientEmail(recipient.getEmail());
        message.setRecipientUserName(recipient.getUserName());
        message.setRecipientDisplayName(recipient.getDisplayName());
        message.setRecipientProfileImageUrl(recipient.getProfileImageUrl());
        message.setContent(cleanedContent);
        message.setTimestamp(Instant.now());

        DirectMessage saved = directMessageRepository.save(message);
        DirectMessageResponse response = toResponse(saved);

        messagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/dm", response);
        return response;
    }

    public DirectMessageResponse deleteMessage(String senderEmail, String messageId) {
        DirectMessage message = directMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        if (!message.getSenderEmail().equals(senderEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own messages");
        }

        message.setDeleted(true);
        DirectMessage saved = directMessageRepository.save(message);
        DirectMessageResponse response = toResponse(saved);

        messagingTemplate.convertAndSendToUser(message.getRecipientEmail(), "/queue/dm.update", response);
        messagingTemplate.convertAndSendToUser(message.getSenderEmail(), "/queue/dm.update", response);
        return response;
    }

    public List<DirectMessageResponse> getHistory(String myEmail, String otherUserId, Instant before, int size) {
        User me = getUserByEmail(myEmail);
        User other = getUserById(otherUserId);

        if (me.getId().equals(other.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot load DM history for yourself");
        }

        if (!areFriends(me, other)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view DM history with friends");
        }

        int normalizedSize = normalizeSize(size);
        String key = conversationKey(me.getId(), other.getId());

        List<DirectMessage> rows;
        if (before == null) {
            rows = directMessageRepository.findByConversationKeyOrderByTimestampDesc(key, PageRequest.of(0, normalizedSize));
        } else {
            rows = directMessageRepository.findByConversationKeyAndTimestampBeforeOrderByTimestampDesc(
                    key,
                    before,
                    PageRequest.of(0, normalizedSize));
        }

        if (rows.isEmpty()) {
            return List.of();
        }

        List<DirectMessage> chronological = new ArrayList<>(rows);
        Collections.reverse(chronological);
        return chronological.stream().map(this::toResponse).toList();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private boolean areFriends(User a, User b) {
        List<String> aFriends = a.getFriendIds();
        List<String> bFriends = b.getFriendIds();
        return aFriends.contains(b.getId()) && bFriends.contains(a.getId());
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_HISTORY_SIZE;
        }
        return Math.min(size, MAX_HISTORY_SIZE);
    }

    private String conversationKey(String id1, String id2) {
        return id1.compareTo(id2) <= 0 ? id1 + "::" + id2 : id2 + "::" + id1;
    }

    private DirectMessageResponse toResponse(DirectMessage message) {
        return new DirectMessageResponse(
                message.getId(),
                message.getConversationKey(),
                message.getSenderId(),
                message.getSenderEmail(),
                message.getSenderUserName(),
                message.getSenderDisplayName(),
                message.getSenderProfileImageUrl(),
                message.getRecipientId(),
                message.getRecipientEmail(),
                message.getRecipientUserName(),
                message.getRecipientDisplayName(),
                message.getRecipientProfileImageUrl(),
                message.isDeleted() ? "This message was deleted" : message.getContent(),
                message.getTimestamp(),
                message.isDeleted());
    }
}

