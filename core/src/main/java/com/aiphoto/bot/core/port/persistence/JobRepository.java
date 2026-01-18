package com.aiphoto.bot.core.port.persistence;

import com.aiphoto.bot.core.domain.Job;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository {

    Job save(Job job);

    Optional<Job> findByOrderId(UUID orderId);

    Optional<Job> findByExternalId(String externalId);
}
