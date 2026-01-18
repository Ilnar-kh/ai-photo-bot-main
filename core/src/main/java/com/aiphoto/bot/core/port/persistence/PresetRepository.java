package com.aiphoto.bot.core.port.persistence;

import com.aiphoto.bot.core.domain.Preset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PresetRepository {

    Optional<Preset> findById(UUID id);

    Optional<Preset> findByName(String name);

    List<Preset> findAll();
}
