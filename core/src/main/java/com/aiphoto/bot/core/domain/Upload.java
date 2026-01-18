package com.aiphoto.bot.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Upload(UUID id, UUID orderId, String objectKey, String contentType, Instant createdAt) {

    public Upload {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(objectKey, "objectKey");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}
