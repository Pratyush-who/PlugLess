package com.example.PlugLess.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.PlugLess.entity.GlobalMessage;

public interface GlobalMessageRepository extends MongoRepository<GlobalMessage, String> {

    // last 50 messages
    List<GlobalMessage> findTop50ByOrderByTimestampDesc();

    //load older messages before a given timestamp
    List<GlobalMessage> findByTimestampBeforeOrderByTimestampDesc(Instant before, Pageable pageable);
}

