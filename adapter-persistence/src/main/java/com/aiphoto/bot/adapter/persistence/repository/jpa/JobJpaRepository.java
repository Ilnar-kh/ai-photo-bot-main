package com.aiphoto.bot.adapter.persistence.repository.jpa;

import com.aiphoto.bot.adapter.persistence.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobJpaRepository extends JpaRepository<JobEntity, UUID> {

    Optional<JobEntity> findByOrderId(UUID orderId);

    Optional<JobEntity> findByExternalId(String externalId);

    void deleteByOrderIdIn(List<UUID> orderIds);
}
