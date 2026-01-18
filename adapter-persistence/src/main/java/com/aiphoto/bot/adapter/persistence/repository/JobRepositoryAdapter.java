package com.aiphoto.bot.adapter.persistence.repository;

import com.aiphoto.bot.adapter.persistence.mapper.JobMapper;
import com.aiphoto.bot.adapter.persistence.repository.jpa.JobJpaRepository;
import com.aiphoto.bot.adapter.persistence.repository.jpa.OrderJpaRepository;
import com.aiphoto.bot.core.domain.Job;
import com.aiphoto.bot.core.port.persistence.JobRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class JobRepositoryAdapter implements JobRepository {

    private final JobJpaRepository jobJpaRepository;
    private final OrderJpaRepository orderJpaRepository;

    public JobRepositoryAdapter(JobJpaRepository jobJpaRepository,
                                OrderJpaRepository orderJpaRepository) {
        this.jobJpaRepository = jobJpaRepository;
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    @Transactional
    public Job save(Job job) {
        return JobMapper.toDomain(jobJpaRepository.save(
            JobMapper.toEntity(job, orderJpaRepository.getReferenceById(job.orderId()))
        ));
    }

    @Override
    public Optional<Job> findByOrderId(UUID orderId) {
        return jobJpaRepository.findByOrderId(orderId).map(JobMapper::toDomain);
    }

    @Override
    public Optional<Job> findByExternalId(String externalId) {
        return jobJpaRepository.findByExternalId(externalId).map(JobMapper::toDomain);
    }
}
