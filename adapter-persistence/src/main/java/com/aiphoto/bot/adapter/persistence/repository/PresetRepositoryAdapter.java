package com.aiphoto.bot.adapter.persistence.repository;

import com.aiphoto.bot.adapter.persistence.mapper.PresetMapper;
import com.aiphoto.bot.adapter.persistence.repository.jpa.PresetJpaRepository;
import com.aiphoto.bot.core.domain.Preset;
import com.aiphoto.bot.core.port.persistence.PresetRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class PresetRepositoryAdapter implements PresetRepository {

    private final PresetJpaRepository presetJpaRepository;

    public PresetRepositoryAdapter(PresetJpaRepository presetJpaRepository) {
        this.presetJpaRepository = presetJpaRepository;
    }

    @Override
    public Optional<Preset> findById(UUID id) {
        return presetJpaRepository.findById(id).map(PresetMapper::toDomain);
    }

    @Override
    public Optional<Preset> findByName(String name) {
        return presetJpaRepository.findByName(name).map(PresetMapper::toDomain);
    }

    @Override
    public List<Preset> findAll() {
        return presetJpaRepository.findAll().stream().map(PresetMapper::toDomain).toList();
    }
}
