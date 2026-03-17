package com.example.PlugLess.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlobalMessageRequest {

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String content;
}

