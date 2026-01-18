package com.aiphoto.bot.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Job(UUID id, UUID orderId, String externalId, Instant createdAt, Instant updatedAt) {

    public Job {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public Job withUpdatedAt(Instant updatedAt) {
        return new Job(id, orderId, externalId, createdAt, updatedAt);
    }
}
