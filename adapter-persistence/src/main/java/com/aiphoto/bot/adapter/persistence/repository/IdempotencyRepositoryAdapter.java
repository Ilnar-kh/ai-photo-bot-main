package com.aiphoto.bot.adapter.persistence.repository;

import com.aiphoto.bot.adapter.persistence.mapper.IdempotencyMapper;
import com.aiphoto.bot.adapter.persistence.repository.jpa.IdempotencyKeyJpaRepository;
import com.aiphoto.bot.core.domain.IdempotencyKey;
import com.aiphoto.bot.core.port.persistence.IdempotencyRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class IdempotencyRepositoryAdapter implements IdempotencyRepository {

    private final IdempotencyKeyJpaRepository idempotencyKeyJpaRepository;

    public IdempotencyRepositoryAdapter(IdempotencyKeyJpaRepository idempotencyKeyJpaRepository) {
        this.idempotencyKeyJpaRepository = idempotencyKeyJpaRepository;
    }

    @Override
    @Transactional
    public IdempotencyKey save(IdempotencyKey key) {
        return IdempotencyMapper.toDomain(idempotencyKeyJpaRepository.save(IdempotencyMapper.toEntity(key)));
    }

    @Override
    public Optional<IdempotencyKey> findByKey(String idempotencyKey) {
        return idempotencyKeyJpaRepository.findByIdempotencyKey(idempotencyKey).map(IdempotencyMapper::toDomain);
    }
}
