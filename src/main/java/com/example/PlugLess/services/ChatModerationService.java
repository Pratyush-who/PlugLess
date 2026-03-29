package com.example.PlugLess.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChatModerationService {

    public static final String BLOCKED_MESSAGE = "Keep the global chat clean wrna ghar se utha lunga..!!";

    private final Set<String> blockedWords;

    public ChatModerationService(
            @Value("classpath:moderation/blocked-words.txt") Resource blockedWordsResource) {
        this.blockedWords = loadBlockedWords(blockedWordsResource);
    }

    public void validateGlobalMessage(String content) {
        if (content == null) {
            return;
        }

        String normalized = content.toLowerCase(Locale.ROOT);
        String[] tokens = normalized.split("[^a-z0-9]+");

        for (String token : tokens) {
            if (!token.isBlank() && blockedWords.contains(token)) {
                throw new GlobalChatModerationException(BLOCKED_MESSAGE);
            }
        }
    }

    private Set<String> loadBlockedWords(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .map(line -> line.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load blocked words list", ex);
        }
    }
}

