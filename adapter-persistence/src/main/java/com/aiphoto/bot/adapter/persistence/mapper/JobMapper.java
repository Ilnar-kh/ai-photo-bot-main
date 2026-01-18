package com.aiphoto.bot.adapter.persistence.mapper;

import com.aiphoto.bot.adapter.persistence.entity.JobEntity;
import com.aiphoto.bot.adapter.persistence.entity.OrderEntity;
import com.aiphoto.bot.core.domain.Job;

public final class JobMapper {

    private JobMapper() {
    }

    public static Job toDomain(JobEntity entity) {
        return new Job(
            entity.getId(),
            entity.getOrder().getId(),
            entity.getExternalId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static JobEntity toEntity(Job job, OrderEntity order) {
        JobEntity entity = new JobEntity();
        entity.setId(job.id());
        entity.setOrder(order);
        entity.setExternalId(job.externalId());
        entity.setCreatedAt(job.createdAt());
        entity.setUpdatedAt(job.updatedAt());
        return entity;
    }
}
