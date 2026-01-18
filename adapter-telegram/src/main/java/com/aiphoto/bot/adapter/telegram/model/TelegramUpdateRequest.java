package com.aiphoto.bot.adapter.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramUpdateRequest(
    @JsonProperty("update_id") @NotNull Long updateId,
    @JsonProperty("message") Map<String, Object> message
) {
}
