package com.aiphoto.bot.core.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record OutboxEvent(UUID id, String aggregateType, UUID aggregateId, Map<String, Object> payload,
                          Instant createdAt, Instant availableAt) {

    public OutboxEvent {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(aggregateType, "aggregateType");
        Objects.requireNonNull(aggregateId, "aggregateId");
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(availableAt, "availableAt");
    }
}
