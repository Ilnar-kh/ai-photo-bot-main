package com.aiphoto.bot.adapter.web.controller;

import com.aiphoto.bot.adapter.web.model.PresetResponse;
import com.aiphoto.bot.core.port.persistence.PresetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/presets")
@Tag(name = "Presets")
public class PresetController {

    private final PresetRepository presetRepository;

    public PresetController(PresetRepository presetRepository) {
        this.presetRepository = presetRepository;
    }

    @GetMapping
    @Operation(summary = "List available presets")
    public List<PresetResponse> listPresets() {
        return presetRepository.findAll().stream()
            .map(preset -> new PresetResponse(preset.id(), preset.name(), preset.model(), preset.parameters()))
            .toList();
    }
}
