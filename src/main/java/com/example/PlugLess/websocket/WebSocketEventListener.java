package com.example.PlugLess.websocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.PlugLess.services.PresenceService;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final ConcurrentHashMap<String, String> sessionEmailMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> emailSessionCounts = new ConcurrentHashMap<>();

    private final PresenceService presenceService;

    public WebSocketEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Object email = accessor.getSessionAttributes() != null
                ? accessor.getSessionAttributes().get("userEmail")
                : null;

        if (sessionId != null && email instanceof String emailStr) {
            sessionEmailMap.put(sessionId, emailStr);
            int activeSessions = emailSessionCounts
                    .computeIfAbsent(emailStr, k -> new AtomicInteger(0))
                    .incrementAndGet();

            log.debug("WebSocket CONNECT: {} (session={}, activeSessions={})", emailStr, sessionId, activeSessions);
            presenceService.markOnline(emailStr);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String email = sessionEmailMap.remove(sessionId);

        if (email != null) {
            AtomicInteger counter = emailSessionCounts.get(email);
            int activeSessions = counter == null ? 0 : counter.decrementAndGet();

            if (activeSessions <= 0) {
                emailSessionCounts.remove(email);
                presenceService.markOffline(email);
            }

            log.debug("WebSocket DISCONNECTED: {} (session={}, activeSessions={})", email, sessionId, Math.max(activeSessions, 0));
        }
    }
}
