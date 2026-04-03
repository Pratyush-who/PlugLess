package com.example.PlugLess.dto.chat.dm;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageResponse {

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
