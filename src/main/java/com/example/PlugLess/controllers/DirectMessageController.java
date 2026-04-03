package com.example.PlugLess.controllers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.example.PlugLess.dto.chat.dm.DirectMessageRequest;
import com.example.PlugLess.dto.chat.dm.DirectMessageDeleteRequest;
import com.example.PlugLess.dto.chat.dm.DirectMessageResponse;
import com.example.PlugLess.services.DirectMessageService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/chat/dm")
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    public DirectMessageController(DirectMessageService directMessageService) {
        this.directMessageService = directMessageService;
    }

    @MessageMapping("/chat.dm.send")
    @SendToUser("/queue/dm")
    public DirectMessageResponse sendMessage(@Valid @Payload DirectMessageRequest request,
                                             SimpMessageHeaderAccessor headerAccessor) {
        String email = sessionEmail(headerAccessor);
        if (email == null) {
            throw new IllegalStateException("Unauthenticated WebSocket message");
        }

        return directMessageService.sendMessage(email, request.getRecipientId(), request.getContent());
    }

    @PostMapping("/send")
    @ResponseBody
    public DirectMessageResponse sendMessageRest(@AuthenticationPrincipal UserDetails userDetails,
                                                 @Valid @RequestBody DirectMessageRequest request) {
        return directMessageService.sendMessage(userDetails.getUsername(), request.getRecipientId(), request.getContent());
    }

    @MessageMapping("/chat.dm.delete")
    @SendToUser("/queue/dm.update")
    public DirectMessageResponse deleteMessage(@Valid @Payload DirectMessageDeleteRequest request,
                                               SimpMessageHeaderAccessor headerAccessor) {
        String email = sessionEmail(headerAccessor);
        if (email == null) {
            throw new IllegalStateException("Unauthenticated WebSocket message");
        }

        return directMessageService.deleteMessage(email, request.getMessageId());
    }

    @PostMapping("/delete")
    @ResponseBody
    public DirectMessageResponse deleteMessageRest(@AuthenticationPrincipal UserDetails userDetails,
                                                   @Valid @RequestBody DirectMessageDeleteRequest request) {
        return directMessageService.deleteMessage(userDetails.getUsername(), request.getMessageId());
    }

    @GetMapping("/{otherUserId}/history")
    @ResponseBody
    public List<DirectMessageResponse> getHistory(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable String otherUserId,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
                                                  @RequestParam(defaultValue = "50") int size) {
        return directMessageService.getHistory(userDetails.getUsername(), otherUserId, before, size);
    }

    @MessageExceptionHandler({ResponseStatusException.class, IllegalStateException.class})
    @SendToUser("/queue/dm-errors")
    public Map<String, String> handleDmError(Exception ex) {
        String message = ex instanceof ResponseStatusException rse && rse.getReason() != null
                ? rse.getReason()
                : ex.getMessage();
        return Map.of("message", message == null ? "Unable to send message" : message);
    }

    private String sessionEmail(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        return sessionAttributes == null ? null : (String) sessionAttributes.get("userEmail");
    }
}
