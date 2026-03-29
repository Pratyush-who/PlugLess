package com.example.PlugLess.services;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class PresenceTrackerService {

    private final ConcurrentHashMap<String, String> sessionEmailMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> emailSessionCounts = new ConcurrentHashMap<>();

    public int registerSession(String sessionId, String email) {
        sessionEmailMap.put(sessionId, email);
        return emailSessionCounts
                .computeIfAbsent(email, key -> new AtomicInteger(0))
                .incrementAndGet();
    }

    public SessionCloseResult unregisterSession(String sessionId) {
        String email = sessionEmailMap.remove(sessionId);
        if (email == null) {
            return new SessionCloseResult(null, 0);
        }

        AtomicInteger counter = emailSessionCounts.get(email);
        int activeSessions = counter == null ? 0 : counter.decrementAndGet();
        if (activeSessions <= 0) {
            emailSessionCounts.remove(email);
            activeSessions = 0;
        }

        return new SessionCloseResult(email, activeSessions);
    }

    public boolean isUserOnline(String email) {
        AtomicInteger count = emailSessionCounts.get(email);
        return count != null && count.get() > 0;
    }

    public long getOnlineCount() {
        return emailSessionCounts.size();
    }

    public Set<String> getOnlineEmails() {
        return Set.copyOf(emailSessionCounts.keySet());
    }

    public record SessionCloseResult(String email, int activeSessions) {
    }
}

