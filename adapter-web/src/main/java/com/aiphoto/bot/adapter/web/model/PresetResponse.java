package com.aiphoto.bot.adapter.web.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

@Schema(name = "Preset")
public record PresetResponse(
    @Schema(description = "Preset identifier") UUID id,
    @Schema(description = "Display name") String name,
    @Schema(description = "Model code") String model,
    @Schema(description = "Generation parameters") Map<String, Object> parameters
) {
}
