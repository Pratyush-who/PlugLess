package com.example.PlugLess.controllers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.PlugLess.dto.chat.GlobalMessageRequest;
import com.example.PlugLess.dto.chat.GlobalMessageResponse;
import com.example.PlugLess.services.GlobalChatService;
import com.example.PlugLess.services.PresenceService;

@Controller
public class GlobalChatController {

    private final GlobalChatService globalChatService;
    private final PresenceService presenceService;

    public GlobalChatController(GlobalChatService globalChatService, PresenceService presenceService) {
        this.globalChatService = globalChatService;
        this.presenceService = presenceService;
    }

    @MessageMapping("/chat.global")
    @SendTo("/topic/global")
    public GlobalMessageResponse sendMessage(
            @Payload GlobalMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        String email = (String) headerAccessor.getSessionAttributes().get("userEmail");
        if (email == null) throw new IllegalStateException("Unauthenticated WebSocket message");

        return globalChatService.saveMessage(email, request.getContent());
    }

    @GetMapping("/chat/global/history")
    @ResponseBody
    public List<GlobalMessageResponse> getHistory(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before) {
        return globalChatService.getHistory(before);
    }

    @GetMapping("/chat/global/stats")
    @ResponseBody
    public Map<String, Long> getStats() {
        return presenceService.getOnlineStats();
    }
}