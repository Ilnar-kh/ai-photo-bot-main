package com.aiphoto.bot.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Image(UUID id, UUID jobId, String objectKey, Instant createdAt) {

    public Image {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(jobId, "jobId");
        Objects.requireNonNull(objectKey, "objectKey");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}
