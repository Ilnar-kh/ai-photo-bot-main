package com.aiphoto.bot.core.port.persistence;

import com.aiphoto.bot.core.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByTelegramId(long telegramId);

    User save(User user);

    Optional<User> findById(UUID id);
}
