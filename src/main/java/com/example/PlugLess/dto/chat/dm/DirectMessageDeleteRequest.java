package com.example.PlugLess.dto.chat.dm;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DirectMessageDeleteRequest {

    @NotBlank(message = "Message ID is required")
    private String messageId;
}

