package com.example.PlugLess.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document("global_messages")
@Getter
@Setter
@NoArgsConstructor
public class GlobalMessage {

    @Id
    private String id;

    private String senderId;
    private String senderUserName;
    private String senderDisplayName;
    private String senderAvatar;

    private String content;

    @Indexed
    private Instant timestamp;
}

