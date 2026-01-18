package com.aiphoto.bot.adapter.persistence.repository.jpa;

import com.aiphoto.bot.adapter.persistence.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageJpaRepository extends JpaRepository<ImageEntity, UUID> {

    List<ImageEntity> findByJobId(UUID jobId);

    void deleteByJob_Order_IdIn(List<UUID> orderIds);
}
