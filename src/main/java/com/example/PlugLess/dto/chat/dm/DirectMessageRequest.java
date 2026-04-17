package com.example.PlugLess.dto.chat.dm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DirectMessageRequest {

    @NotBlank
    @JsonAlias({"recipientUserId", "receiverId", "otherUserId"})
    private String recipientId;

    @NotBlank
    @Size(max = 1000)
    @JsonAlias({"text", "message", "msg", "content"})
    private String content;
}
