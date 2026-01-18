package com.aiphoto.bot.adapter.persistence.mapper;

import com.aiphoto.bot.adapter.persistence.entity.OrderEntity;
import com.aiphoto.bot.adapter.persistence.entity.PresetEntity;
import com.aiphoto.bot.adapter.persistence.entity.UserEntity;
import com.aiphoto.bot.core.domain.Order;
import com.aiphoto.bot.core.domain.OrderStatus;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static Order toDomain(OrderEntity entity) {
        return new Order(
                entity.getId(),
                entity.getUser().getId(),
                entity.getPreset().getId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getIdentityPath(),
                entity.getLoraPath(),
                entity.getGender(),
                entity.getTrainingRequestId(),
                entity.getLoraTrainedAt(),
                entity.getLoraStatus(),
                entity.getLoraConfigUrl(),
                entity.isReadyHintSent(),
                entity.getUsedPhotos(),
                entity.getPhotosLimit(),

                // ✅ НОВОЕ: добивки
                entity.getOfferShownAt(),
                entity.getPurchasedAt(),
                entity.getFollowup30SentAt(),
                entity.getFollowup24SentAt()
        );
    }

    public static OrderEntity toEntity(Order order, UserEntity user, PresetEntity preset) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.id());
        entity.setUser(user);
        entity.setPreset(preset);
        entity.setStatus(order.status());
        entity.setCreatedAt(order.createdAt());
        entity.setUpdatedAt(order.updatedAt());
        entity.setIdentityPath(order.identityPath());
        entity.setLoraPath(order.loraPath());
        entity.setGender(order.gender());
        entity.setTrainingRequestId(order.trainingRequestId());
        entity.setLoraTrainedAt(order.loraTrainedAt());
        entity.setLoraStatus(order.loraStatus());
        entity.setLoraConfigUrl(order.loraConfigUrl());
        entity.setReadyHintSent(order.readyHintSent());

        entity.setUsedPhotos(order.usedPhotos());
        entity.setPhotosLimit(order.photosLimit());

        // ✅ НОВОЕ: добивки
        entity.setOfferShownAt(order.offerShownAt());
        entity.setPurchasedAt(order.purchasedAt());
        entity.setFollowup30SentAt(order.followup30SentAt());
        entity.setFollowup24SentAt(order.followup24SentAt());

        return entity;
    }

    public static void updateStatus(OrderEntity entity, OrderStatus status, java.time.Instant updatedAt) {
        entity.setStatus(status);
        entity.setUpdatedAt(updatedAt);
    }
}