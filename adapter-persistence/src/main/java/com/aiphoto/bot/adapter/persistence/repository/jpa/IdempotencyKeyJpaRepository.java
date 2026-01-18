package com.aiphoto.bot.adapter.persistence.repository.jpa;

import com.aiphoto.bot.adapter.persistence.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyEntity, UUID> {

    Optional<IdempotencyKeyEntity> findByIdempotencyKey(String idempotencyKey);
}
