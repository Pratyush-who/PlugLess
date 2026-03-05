package com.example.PlugLess.config;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.PlugLess.security.JwtService;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    public WebSocketConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Clients subscribe to /topic/... (broadcast) and /queue/... (user-specific DMs)
        registry.enableSimpleBroker("/topic", "/queue");
        // Messages sent from client use /app/... prefix
        registry.setApplicationDestinationPrefixes("/app");
        // For user-specific destinations like /user/{userId}/queue/messages
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor())
                .withSockJS();

        // Also register without SockJS for native STOMP clients (Flutter uses this)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor());
    }

    private HandshakeInterceptor jwtHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attributes) {
                String query = request.getURI().getQuery();
                String token = null;

                if (query != null) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("token=")) {
                            token = param.substring(6);
                            break;
                        }
                    }
                }

                if (token == null) {
                    List<String> authHeaders = request.getHeaders().get("Authorization");
                    if (authHeaders != null && !authHeaders.isEmpty()) {
                        String header = authHeaders.get(0);
                        if (header.startsWith("Bearer ")) {
                            token = header.substring(7);
                        }
                    }
                }

                if (token != null && jwtService.isTokenValid(token)) {
                    String email = jwtService.extractEmail(token);
                    // Store email in WS session — available in connect/disconnect events
                    attributes.put("userEmail", email);
                    return true;
                }

                return false;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Exception exception) {
            }
        };
    }
}

