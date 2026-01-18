package com.aiphoto.bot.adapter.persistence.repository.jpa;

import com.aiphoto.bot.adapter.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByTelegramId(long telegramId);
}
