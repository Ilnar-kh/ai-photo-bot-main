package com.aiphoto.bot.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record User(UUID id, long telegramId, String username, Instant createdAt) {

    public User {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public User withUsername(String newUsername) {
        return new User(id, telegramId, newUsername, createdAt);
    }
}
