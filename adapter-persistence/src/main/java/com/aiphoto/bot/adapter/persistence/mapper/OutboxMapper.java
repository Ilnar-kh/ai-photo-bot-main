package com.aiphoto.bot.adapter.persistence.mapper;

import com.aiphoto.bot.adapter.persistence.entity.OutboxEventEntity;
import com.aiphoto.bot.core.domain.OutboxEvent;

import java.util.Map;

public final class OutboxMapper {

    private OutboxMapper() {
    }

    public static OutboxEvent toDomain(OutboxEventEntity entity) {
        return new OutboxEvent(
                entity.getId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                Map.copyOf(entity.getPayload() == null ? Map.of() : entity.getPayload()),
                entity.getCreatedAt(),
                entity.getAvailableAt()
        );
    }

    public static OutboxEventEntity toEntity(OutboxEvent event) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(event.id());
        entity.setAggregateType(event.aggregateType());
        entity.setAggregateId(event.aggregateId());
        entity.setPayload(event.payload());
        entity.setCreatedAt(event.createdAt());
        entity.setAvailableAt(event.availableAt());
        return entity;
    }
}