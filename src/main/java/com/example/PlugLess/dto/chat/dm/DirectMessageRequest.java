package com.example.PlugLess.dto.chat.dm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DirectMessageRequest {

    @NotBlank
    private String recipientId;

    @NotBlank
    @Size(max = 1000)
    private String content;
}

