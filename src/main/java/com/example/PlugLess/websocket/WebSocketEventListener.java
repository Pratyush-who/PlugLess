package com.example.PlugLess.websocket;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.PlugLess.services.PresenceService;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final ConcurrentHashMap<String, String> sessionEmailMap = new ConcurrentHashMap<>();

    private final PresenceService presenceService;

    public WebSocketEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Object email = accessor.getSessionAttributes() != null
                ? accessor.getSessionAttributes().get("userEmail")
                : null;

        if (sessionId != null && email instanceof String emailStr) {
            sessionEmailMap.put(sessionId, emailStr);
            log.debug("WebSocket CONNECTED: {} (session={})", emailStr, sessionId);
            presenceService.markOnline(emailStr);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String email = sessionEmailMap.remove(sessionId);

        if (email != null) {
            log.debug("WebSocket DISCONNECTED: {} (session={})", email, sessionId);
            presenceService.markOffline(email);
        }
    }
}
