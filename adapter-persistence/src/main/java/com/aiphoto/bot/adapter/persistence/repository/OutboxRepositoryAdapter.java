package com.aiphoto.bot.adapter.persistence.repository;

import com.aiphoto.bot.adapter.persistence.mapper.OutboxMapper;
import com.aiphoto.bot.adapter.persistence.repository.jpa.OutboxJpaRepository;
import com.aiphoto.bot.core.domain.OutboxEvent;
import com.aiphoto.bot.core.port.persistence.OutboxRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class OutboxRepositoryAdapter implements OutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;

    public OutboxRepositoryAdapter(OutboxJpaRepository outboxJpaRepository) {
        this.outboxJpaRepository = outboxJpaRepository;
    }

    @Override
    @Transactional
    public OutboxEvent save(OutboxEvent event) {
        return OutboxMapper.toDomain(outboxJpaRepository.save(OutboxMapper.toEntity(event)));
    }
}
