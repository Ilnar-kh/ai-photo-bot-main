package com.aiphoto.bot.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Order(
        UUID id,
        UUID userId,
        UUID presetId,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt,
        String identityPath,
        String loraPath,
        Gender gender,
        String trainingRequestId,
        Instant loraTrainedAt,
        LoraStatus loraStatus,
        String loraConfigUrl,
        Boolean readyHintSent,

        // === НОВОЕ: лимиты генераций ===
        Integer usedPhotos,     // сколько фото сгенерировал
        Integer photosLimit,    // лимит фото (начально 60)

        // === НОВОЕ: маркетинговые добивки ===
        Instant offerShownAt,        // когда показали оффер после "Далее"
        Instant purchasedAt,         // когда купил
        Instant followup30SentAt,    // когда отправили добивку через 30 минут
        Instant followup24SentAt     // когда отправили добивку через 24 часа
) {

    public Order {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(presetId, "presetId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");

        loraStatus = (loraStatus == null) ? LoraStatus.NONE : loraStatus;
        readyHintSent = (readyHintSent != null) && readyHintSent;

        // === значения по умолчанию для лимитов ===
        usedPhotos = (usedPhotos == null) ? 0 : usedPhotos;
        photosLimit = (photosLimit == null) ? 60 : photosLimit;
        // для новых полей добивок дефолт не нужен (null = не было)
    }

    // ——— КОРОТКИЙ конструктор (без лимитов/добивок)
    public Order(UUID id,
                 UUID userId,
                 UUID presetId,
                 OrderStatus status,
                 Instant createdAt,
                 Instant updatedAt,
                 String identityPath,
                 String loraPath) {
        this(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, loraPath,
                null,        // gender
                null,        // trainingRequestId
                null,        // loraTrainedAt
                LoraStatus.NONE,
                null,        // loraConfigUrl
                false,       // readyHintSent
                0,           // usedPhotos
                60,          // photosLimit
                null,        // offerShownAt
                null,        // purchasedAt
                null,        // followup30SentAt
                null         // followup24SentAt
        );
    }

    // ——— ВЕСЬ ТВОЙ КОД НИЖЕ ОСТАЁТСЯ БЕЗ ИЗМЕНЕНИЙ ———

    public Order withStatus(OrderStatus newStatus, Instant updatedAt) {
        return new Order(id, userId, presetId, newStatus, createdAt, updatedAt,
                identityPath, loraPath, gender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, readyHintSent,
                usedPhotos, photosLimit,
                offerShownAt, purchasedAt, followup30SentAt, followup24SentAt);
    }

    public Order withIdentityPath(String newIdentityPath, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                newIdentityPath, loraPath, gender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, readyHintSent,
                usedPhotos, photosLimit,
                offerShownAt, purchasedAt, followup30SentAt, followup24SentAt);
    }

    public Order withLoraPath(String newLoraPath, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, newLoraPath, gender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, readyHintSent,
                usedPhotos, photosLimit,
                offerShownAt, purchasedAt, followup30SentAt, followup24SentAt);
    }

    public Order withGender(Gender newGender, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, loraPath, newGender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, readyHintSent,
                usedPhotos, photosLimit,
                offerShownAt, purchasedAt, followup30SentAt, followup24SentAt);
    }

    public Order withTrainingSubmission(String requestId, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, loraPath, gender, requestId, null,
                LoraStatus.SUBMITTED, loraConfigUrl, readyHintSent,
                usedPhotos, photosLimit,
                offerShownAt, purchasedAt, followup30SentAt, followup24SentAt);
    }

    public Order withLoraReady(String newLoraPath, String configUrl, Instant trainedAt, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, newLoraPath, gender, trainingRequestId, trainedAt,
                LoraStatus.READY, configUrl, readyHintSent,
                usedPhotos, photosLimit,
                offerShownAt, purchasedAt, followup30SentAt, followup24SentAt);
    }

    public Order withLoraFailed(Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, loraPath, gender, trainingRequestId, loraTrainedAt,
                LoraStatus.FAILED, loraConfigUrl, readyHintSent,
                usedPhotos, photosLimit,
                offerShownAt, purchasedAt, followup30SentAt, followup24SentAt);
    }

    public Order withReadyHintSent(boolean value, Instant now) {
        return new Order(id, userId, presetId, status, createdAt, now,
                identityPath, loraPath, gender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, value,
                usedPhotos, photosLimit,
                offerShownAt, purchasedAt, followup30SentAt, followup24SentAt);
    }

    // === НОВЫЕ ВОЗВРАТНЫЕ РОЗЕТОЧКИ (лимиты) ===

    public Order withUsedPhotos(int newUsedPhotos, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, loraPath, gender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, readyHintSent,
                newUsedPhotos, photosLimit,
                offerShownAt, purchasedAt, followup30SentAt, followup24SentAt);
    }

    public Order withPhotosLimit(int newLimit, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, loraPath, gender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, readyHintSent,
                usedPhotos, newLimit,
                offerShownAt, purchasedAt, followup30SentAt, followup24SentAt);
    }

    // === НОВЫЕ ВОЗВРАТНЫЕ РОЗЕТОЧКИ (добивки) ===

    public Order withOfferShownAt(Instant at, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, loraPath, gender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, readyHintSent,
                usedPhotos, photosLimit,
                at, purchasedAt, followup30SentAt, followup24SentAt);
    }

    public Order withPurchasedAt(Instant at, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, loraPath, gender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, readyHintSent,
                usedPhotos, photosLimit,
                offerShownAt, at, followup30SentAt, followup24SentAt);
    }

    public Order withFollowup30SentAt(Instant at, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, loraPath, gender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, readyHintSent,
                usedPhotos, photosLimit,
                offerShownAt, purchasedAt, at, followup24SentAt);
    }

    public Order withFollowup24SentAt(Instant at, Instant updatedAt) {
        return new Order(id, userId, presetId, status, createdAt, updatedAt,
                identityPath, loraPath, gender, trainingRequestId, loraTrainedAt,
                loraStatus, loraConfigUrl, readyHintSent,
                usedPhotos, photosLimit,
                offerShownAt, purchasedAt, followup30SentAt, at);
    }

    public Boolean readyHintSent() {
        return Boolean.TRUE.equals(readyHintSent);
    }
}