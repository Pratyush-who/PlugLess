package com.example.PlugLess.dto.chat;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GlobalMessageResponse {

    private String id;
    private String senderId;
    private String senderUserName;
    private String senderDisplayName;
    private String senderAvatar;
    private String content;
    private Instant timestamp;
}

