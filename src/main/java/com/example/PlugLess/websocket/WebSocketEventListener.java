package com.example.PlugLess.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.PlugLess.services.PresenceService;
import com.example.PlugLess.services.PresenceTrackerService;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final PresenceService presenceService;
    private final PresenceTrackerService presenceTrackerService;

    public WebSocketEventListener(PresenceService presenceService, PresenceTrackerService presenceTrackerService) {
        this.presenceService = presenceService;
        this.presenceTrackerService = presenceTrackerService;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Object email = accessor.getSessionAttributes() != null
                ? accessor.getSessionAttributes().get("userEmail")
                : null;

        if (sessionId != null && email instanceof String emailStr) {
            int activeSessions = presenceTrackerService.registerSession(sessionId, emailStr);

            log.debug("WebSocket CONNECT: {} (session={}, activeSessions={})", emailStr, sessionId, activeSessions);
            presenceService.markOnline(emailStr);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        PresenceTrackerService.SessionCloseResult closeResult = presenceTrackerService.unregisterSession(sessionId);
        String email = closeResult.email();

        if (email != null) {
            int activeSessions = closeResult.activeSessions();

            if (activeSessions <= 0) {
                presenceService.markOffline(email);
            }

            log.debug("WebSocket DISCONNECTED: {} (session={}, activeSessions={})", email, sessionId, Math.max(activeSessions, 0));
        }
    }
}
