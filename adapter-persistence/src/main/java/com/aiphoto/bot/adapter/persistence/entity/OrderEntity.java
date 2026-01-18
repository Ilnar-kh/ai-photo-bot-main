package com.aiphoto.bot.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_status", columnList = "user_id,status"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
})
public class OrderEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_user"))
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preset_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_preset"))
    private PresetEntity preset;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private com.aiphoto.bot.core.domain.OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "identity_path")
    private String identityPath;

    @Column(name = "lora_path")
    private String loraPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private com.aiphoto.bot.core.domain.Gender gender;

    @Column(name = "training_request_id")
    private String trainingRequestId;

    @Column(name = "lora_trained_at")
    private Instant loraTrainedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "lora_status", nullable = false)
    private com.aiphoto.bot.core.domain.LoraStatus loraStatus = com.aiphoto.bot.core.domain.LoraStatus.NONE;

    @Column(name = "lora_config_url")
    private String loraConfigUrl;

    @Column(name = "ready_hint_sent", nullable = false)
    private boolean readyHintSent;

    @Column(name = "used_photos", nullable = false)
    private int usedPhotos = 0;

    @Column(name = "photos_limit", nullable = false)
    private int photosLimit = 60;

    // =====================================================================
    // НОВОЕ: поля для маркетинговых добивок
    // =====================================================================

    @Column(name = "offer_shown_at")
    private Instant offerShownAt;

    @Column(name = "purchased_at")
    private Instant purchasedAt;

    @Column(name = "followup30_sent_at")
    private Instant followup30SentAt;

    @Column(name = "followup24_sent_at")
    private Instant followup24SentAt;

    // =====================================================================
    // getters / setters (существующие + новые)
    // =====================================================================

    public int getUsedPhotos() {
        return usedPhotos;
    }

    public void setUsedPhotos(int usedPhotos) {
        this.usedPhotos = usedPhotos;
    }

    public int getPhotosLimit() {
        return photosLimit;
    }

    public void setPhotosLimit(int photosLimit) {
        this.photosLimit = photosLimit;
    }

    public boolean isReadyHintSent() {
        return readyHintSent;
    }

    public void setReadyHintSent(boolean readyHintSent) {
        this.readyHintSent = readyHintSent;
    }

    public Instant getOfferShownAt() {
        return offerShownAt;
    }

    public void setOfferShownAt(Instant offerShownAt) {
        this.offerShownAt = offerShownAt;
    }

    public Instant getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(Instant purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public Instant getFollowup30SentAt() {
        return followup30SentAt;
    }

    public void setFollowup30SentAt(Instant followup30SentAt) {
        this.followup30SentAt = followup30SentAt;
    }

    public Instant getFollowup24SentAt() {
        return followup24SentAt;
    }

    public void setFollowup24SentAt(Instant followup24SentAt) {
        this.followup24SentAt = followup24SentAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public PresetEntity getPreset() {
        return preset;
    }

    public void setPreset(PresetEntity preset) {
        this.preset = preset;
    }

    public com.aiphoto.bot.core.domain.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(com.aiphoto.bot.core.domain.OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getIdentityPath() {
        return identityPath;
    }

    public void setIdentityPath(String identityPath) {
        this.identityPath = identityPath;
    }

    public String getLoraPath() {
        return loraPath;
    }

    public void setLoraPath(String loraPath) {
        this.loraPath = loraPath;
    }

    public com.aiphoto.bot.core.domain.Gender getGender() {
        return gender;
    }

    public void setGender(com.aiphoto.bot.core.domain.Gender gender) {
        this.gender = gender;
    }

    public String getTrainingRequestId() {
        return trainingRequestId;
    }

    public void setTrainingRequestId(String trainingRequestId) {
        this.trainingRequestId = trainingRequestId;
    }

    public Instant getLoraTrainedAt() {
        return loraTrainedAt;
    }

    public void setLoraTrainedAt(Instant loraTrainedAt) {
        this.loraTrainedAt = loraTrainedAt;
    }

    public com.aiphoto.bot.core.domain.LoraStatus getLoraStatus() {
        return loraStatus;
    }

    public void setLoraStatus(com.aiphoto.bot.core.domain.LoraStatus loraStatus) {
        this.loraStatus = loraStatus;
    }

    public String getLoraConfigUrl() {
        return loraConfigUrl;
    }

    public void setLoraConfigUrl(String loraConfigUrl) {
        this.loraConfigUrl = loraConfigUrl;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}