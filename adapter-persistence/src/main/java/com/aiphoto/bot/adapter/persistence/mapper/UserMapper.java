package com.aiphoto.bot.adapter.persistence.mapper;

import com.aiphoto.bot.adapter.persistence.entity.UserEntity;
import com.aiphoto.bot.core.domain.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserEntity entity) {
        return new User(
            entity.getId(),
            entity.getTelegramId(),
            entity.getUsername(),
            entity.getCreatedAt()
        );
    }

    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.id());
        entity.setTelegramId(user.telegramId());
        entity.setUsername(user.username());
        entity.setCreatedAt(user.createdAt());
        return entity;
    }
}
