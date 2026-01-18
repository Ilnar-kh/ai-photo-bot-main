package com.aiphoto.bot.adapter.persistence.repository;

import com.aiphoto.bot.adapter.persistence.mapper.UserMapper;
import com.aiphoto.bot.adapter.persistence.repository.jpa.UserJpaRepository;
import com.aiphoto.bot.core.domain.User;
import com.aiphoto.bot.core.port.persistence.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<User> findByTelegramId(long telegramId) {
        return userJpaRepository.findByTelegramId(telegramId).map(UserMapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        return UserMapper.toDomain(userJpaRepository.save(UserMapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(UserMapper::toDomain);
    }
}
