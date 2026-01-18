package com.aiphoto.bot.adapter.persistence.repository;

import com.aiphoto.bot.adapter.persistence.entity.OrderEntity;
import com.aiphoto.bot.adapter.persistence.mapper.OrderMapper;
import com.aiphoto.bot.adapter.persistence.repository.jpa.*;
import com.aiphoto.bot.core.domain.LoraStatus;
import com.aiphoto.bot.core.domain.Order;
import com.aiphoto.bot.core.port.persistence.OrderRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PresetJpaRepository presetJpaRepository;

    private final UploadJpaRepository uploadJpaRepository;
    private final ImageJpaRepository imageJpaRepository;
    private final JobJpaRepository jobJpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository orderJpaRepository,
                                  UserJpaRepository userJpaRepository,
                                  PresetJpaRepository presetJpaRepository,
                                  UploadJpaRepository uploadJpaRepository,
                                  ImageJpaRepository imageJpaRepository,
                                  JobJpaRepository jobJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.presetJpaRepository = presetJpaRepository;
        this.uploadJpaRepository = uploadJpaRepository;
        this.imageJpaRepository = imageJpaRepository;
        this.jobJpaRepository = jobJpaRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = OrderMapper.toEntity(order,
                userJpaRepository.getReferenceById(order.userId()),
                presetJpaRepository.getReferenceById(order.presetId()));
        return OrderMapper.toDomain(orderJpaRepository.save(entity));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderJpaRepository.findById(id).map(OrderMapper::toDomain);
    }

    @Override
    public Optional<Order> findLatestByUserId(UUID userId) {
        return orderJpaRepository.findTopByUser_IdOrderByCreatedAtDesc(userId)
                .map(OrderMapper::toDomain);
    }

    @Override
    public List<Order> findByLoraStatus(LoraStatus status) {
        return orderJpaRepository.findByLoraStatus(status).stream()
                .map(OrderMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Order> findLatestReadyByUserId(UUID userId) {
        return orderJpaRepository
                .findTopByUser_IdAndLoraStatusOrderByCreatedAtDesc(userId, LoraStatus.READY)
                .map(OrderMapper::toDomain);
    }

    @Override
    @Transactional
    public void purgeUserDataForRetrain(long telegramId, UUID orderIdToKeep) {
        userJpaRepository.findByTelegramId(telegramId).ifPresent(user -> {
            List<OrderEntity> allOrders = orderJpaRepository.findByUser_Id(user.getId());

            List<UUID> oldOrderIds = allOrders.stream()
                    .map(OrderEntity::getId)
                    .filter(id -> !id.equals(orderIdToKeep))
                    .toList();

            if (!oldOrderIds.isEmpty()) {
                uploadJpaRepository.deleteByOrderIdIn(oldOrderIds);
                imageJpaRepository.deleteByJob_Order_IdIn(oldOrderIds);
                jobJpaRepository.deleteByOrderIdIn(oldOrderIds);
                orderJpaRepository.deleteByIdIn(oldOrderIds);
            }

            orderJpaRepository.findById(orderIdToKeep).ifPresent(order -> {
                order.setLoraStatus(com.aiphoto.bot.core.domain.LoraStatus.NONE);
                order.setLoraPath(null);
                order.setLoraConfigUrl(null);
                order.setTrainingRequestId(null);
                order.setLoraTrainedAt(null);
                order.setReadyHintSent(false);
                order.setIdentityPath(null);

                order.setUpdatedAt(Instant.now());
                orderJpaRepository.save(order);
            });
        });
    }

    @Override
    @Transactional
    public void incrementUsedPhotos(UUID orderId, int delta) {
        if (delta <= 0) return;
        int updated = orderJpaRepository.incrementUsedPhotos(orderId, delta, Instant.now());
        if (updated == 0) throw new IllegalStateException("Order not found: " + orderId);
    }

    @Override
    @Transactional
    public void increasePhotosLimit(UUID orderId, int delta) {
        if (delta <= 0) return;
        int updated = orderJpaRepository.increasePhotosLimit(orderId, delta, Instant.now());
        if (updated == 0) throw new IllegalStateException("Order not found: " + orderId);
    }

    @Override
    @Transactional
    public boolean tryReservePhotos(UUID orderId, int delta) {
        if (delta <= 0) return false;
        int updated = orderJpaRepository.reservePhotos(orderId, delta, Instant.now());
        return updated == 1;
    }
    // =====================================================================
    // НОВОЕ: метки для "добивок" продаж (ИДЕМПОТЕНТНО)
    // =====================================================================

    @Override
    @Transactional
    public void markOfferShownIfNull(UUID orderId, Instant now) {
        orderJpaRepository.setOfferShownAtIfNull(orderId, now, now);
    }

    @Override
    @Transactional
    public void markPurchased(UUID orderId, Instant now) {
        int updated = orderJpaRepository.setPurchasedAt(orderId, now, now);
        if (updated == 0) throw new IllegalStateException("Order not found: " + orderId);
    }

    @Override
    @Transactional
    public void markFollowup30Sent(UUID orderId, Instant now) {
        orderJpaRepository.setFollowup30SentAtIfNull(orderId, now, now);
        // 0 — нормально при повторном запуске (уже отправляли)
    }

    @Override
    @Transactional
    public void markFollowup24Sent(UUID orderId, Instant now) {
        orderJpaRepository.setFollowup24SentAtIfNull(orderId, now, now);
        // 0 — нормально при повторном запуске (уже отправляли)
    }
}