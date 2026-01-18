package com.aiphoto.bot.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record IdempotencyKey(
        UUID id,
        String idempotencyKey,
        String requestHash,
        Instant createdAt,
        Instant expiresAt,
        String responseBody // может быть null
) {
    public IdempotencyKey {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        Objects.requireNonNull(requestHash, "requestHash");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
    }

    public Optional<String> responseBodyOpt() {
        return Optional.ofNullable(responseBody);
    }
}
