package com.aiphoto.bot.core.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record Preset(UUID id, String name, String model, Map<String, Object> parameters, Instant createdAt) {

    public Preset {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(parameters, "parameters");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}
