package com.aiphoto.bot.adapter.persistence.mapper;

import com.aiphoto.bot.adapter.persistence.entity.IdempotencyKeyEntity;
import com.aiphoto.bot.core.domain.IdempotencyKey;

public final class IdempotencyMapper {

    private IdempotencyMapper() {
    }

    public static IdempotencyKey toDomain(IdempotencyKeyEntity entity) {
        return new IdempotencyKey(
            entity.getId(),
            entity.getIdempotencyKey(),
            entity.getRequestHash(),
            entity.getCreatedAt(),
            entity.getExpiresAt(),
            entity.getResponseBody()
        );
    }

    public static IdempotencyKeyEntity toEntity(IdempotencyKey key) {
        IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
        entity.setId(key.id());
        entity.setIdempotencyKey(key.idempotencyKey());
        entity.setRequestHash(key.requestHash());
        entity.setCreatedAt(key.createdAt());
        entity.setExpiresAt(key.expiresAt());
        entity.setResponseBody(key.responseBodyOpt().orElse(null));
        return entity;
    }
}
