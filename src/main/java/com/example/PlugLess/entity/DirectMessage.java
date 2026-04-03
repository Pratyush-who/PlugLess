package com.example.PlugLess.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document("direct_messages")
@CompoundIndexes({
    @CompoundIndex(name = "dm_conversation_timestamp_idx", def = "{'conversationKey': 1, 'timestamp': -1}")
})
@Getter
@Setter
@NoArgsConstructor
public class DirectMessage {

    @Id
    private String id;

    private String conversationKey;

    private String senderId;
    private String senderEmail;
    private String senderUserName;
    private String senderDisplayName;
    private String senderProfileImageUrl;

    private String recipientId;
    private String recipientEmail;
    private String recipientUserName;
    private String recipientDisplayName;
    private String recipientProfileImageUrl;

    private String content;
    private Instant timestamp;
    private boolean deleted;
}
