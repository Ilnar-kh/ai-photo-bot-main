package com.aiphoto.bot.core.port.persistence;

import com.aiphoto.bot.core.domain.LoraStatus;
import com.aiphoto.bot.core.domain.Order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    Optional<Order> findLatestByUserId(UUID userId);

    List<Order> findByLoraStatus(LoraStatus status);

    Optional<Order> findLatestReadyByUserId(UUID userId);

    void purgeUserDataForRetrain(long telegramId, UUID orderIdToKeep);
    // реализуй как SELECT ... WHERE user_id=? AND lora_status='READY' ORDER BY created_at DESC LIMIT 1

    void incrementUsedPhotos(UUID orderId, int delta);

    void increasePhotosLimit(UUID orderId, int delta);

    boolean tryReservePhotos(UUID orderId, int delta);

    // =====================================================================
    // НОВОЕ: метки для маркетинговых добивок
    // =====================================================================

    /**
     * Пометить, что оффер был показан (если ещё не был).
     * Используется при нажатии "Далее".
     */
    void markOfferShownIfNull(UUID orderId, Instant now);

    /**
     * Пометить, что заказ был оплачен.
     * Используется при успешной оплате.
     */
    void markPurchased(UUID orderId, Instant now);

    /**
     * Пометить, что отправлена добивка через 30 минут.
     */
    void markFollowup30Sent(UUID orderId, Instant now);

    /**
     * Пометить, что отправлена добивка через 24 часа.
     */
    void markFollowup24Sent(UUID orderId, Instant now);
}

