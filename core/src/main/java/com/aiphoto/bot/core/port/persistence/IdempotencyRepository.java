package com.aiphoto.bot.core.port.persistence;

import com.aiphoto.bot.core.domain.IdempotencyKey;

import java.util.Optional;

public interface IdempotencyRepository {

    IdempotencyKey save(IdempotencyKey key);

    Optional<IdempotencyKey> findByKey(String idempotencyKey);
}
