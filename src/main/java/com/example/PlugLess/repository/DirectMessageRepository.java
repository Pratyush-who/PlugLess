package com.example.PlugLess.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.PlugLess.entity.DirectMessage;

public interface DirectMessageRepository extends MongoRepository<DirectMessage, String> {

    List<DirectMessage> findByConversationKeyOrderByTimestampDesc(String conversationKey, Pageable pageable);

    List<DirectMessage> findByConversationKeyAndTimestampBeforeOrderByTimestampDesc(
            String conversationKey,
            Instant before,
            Pageable pageable);
}

