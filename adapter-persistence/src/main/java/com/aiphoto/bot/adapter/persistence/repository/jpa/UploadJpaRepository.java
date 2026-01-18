package com.aiphoto.bot.adapter.persistence.repository.jpa;

import com.aiphoto.bot.adapter.persistence.entity.UploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UploadJpaRepository extends JpaRepository<UploadEntity, UUID> {

    List<UploadEntity> findByOrderId(UUID orderId);

    void deleteByOrderIdIn(List<UUID> orderIds);
}
