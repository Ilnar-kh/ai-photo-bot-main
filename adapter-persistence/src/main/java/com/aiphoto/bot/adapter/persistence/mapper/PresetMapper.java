package com.aiphoto.bot.adapter.persistence.mapper;

import com.aiphoto.bot.adapter.persistence.entity.PresetEntity;
import com.aiphoto.bot.core.domain.Preset;

import java.util.Map;

public final class PresetMapper {

    private PresetMapper() {
    }

    public static Preset toDomain(PresetEntity entity) {
        return new Preset(
                entity.getId(),
                entity.getName(),
                entity.getModel(),
                Map.copyOf(entity.getParamsJson() == null ? Map.of() : entity.getParamsJson()),
                entity.getCreatedAt()
        );
    }

    public static PresetEntity toEntity(Preset preset) {
        PresetEntity entity = new PresetEntity();
        entity.setId(preset.id());
        entity.setName(preset.name());
        entity.setModel(preset.model());
        entity.setParamsJson(preset.parameters());
        entity.setCreatedAt(preset.createdAt());
        return entity;
    }
}