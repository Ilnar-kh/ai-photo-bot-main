package com.aiphoto.bot.adapter.persistence.repository;

import com.aiphoto.bot.adapter.persistence.mapper.UploadMapper;
import com.aiphoto.bot.adapter.persistence.repository.jpa.OrderJpaRepository;
import com.aiphoto.bot.adapter.persistence.repository.jpa.UploadJpaRepository;
import com.aiphoto.bot.core.domain.Upload;
import com.aiphoto.bot.core.port.persistence.UploadRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class UploadRepositoryAdapter implements UploadRepository {

    private final UploadJpaRepository uploadJpaRepository;
    private final OrderJpaRepository orderJpaRepository;

    public UploadRepositoryAdapter(UploadJpaRepository uploadJpaRepository,
                                   OrderJpaRepository orderJpaRepository) {
        this.uploadJpaRepository = uploadJpaRepository;
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    @Transactional
    public Upload save(Upload upload) {
        return UploadMapper.toDomain(uploadJpaRepository.save(
            UploadMapper.toEntity(upload, orderJpaRepository.getReferenceById(upload.orderId()))
        ));
    }

    @Override
    public List<Upload> findByOrderId(UUID orderId) {
        return uploadJpaRepository.findByOrderId(orderId).stream().map(UploadMapper::toDomain).toList();
    }
}
