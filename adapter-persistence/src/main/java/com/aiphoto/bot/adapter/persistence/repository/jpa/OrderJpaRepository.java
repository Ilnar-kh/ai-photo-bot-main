package com.aiphoto.bot.adapter.persistence.repository.jpa;

import com.aiphoto.bot.adapter.persistence.entity.OrderEntity;
import com.aiphoto.bot.core.domain.LoraStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findTopByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<OrderEntity> findByLoraStatus(LoraStatus status);

    Optional<OrderEntity> findTopByUser_IdAndLoraStatusOrderByCreatedAtDesc(UUID userId, LoraStatus loraStatus);

    List<OrderEntity> findByUser_Id(UUID userId);

    void deleteByIdIn(List<UUID> ids);

    // ✅ АТОМАРНОЕ увеличение used_photos
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update OrderEntity o
                   set o.usedPhotos = o.usedPhotos + :delta,
                       o.updatedAt = :now
                 where o.id = :orderId
            """)
    int incrementUsedPhotos(@Param("orderId") UUID orderId,
                            @Param("delta") int delta,
                            @Param("now") Instant now);

    // ✅ АТОМАРНОЕ увеличение photos_limit
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update OrderEntity o
                   set o.photosLimit = o.photosLimit + :delta,
                       o.updatedAt = :now
                 where o.id = :orderId
            """)
    int increasePhotosLimit(@Param("orderId") UUID orderId,
                            @Param("delta") int delta,
                            @Param("now") Instant now);

    // ✅ АТОМАРНОЕ "резервирование" генераций (used + delta <= limit)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update OrderEntity o
                   set o.usedPhotos = o.usedPhotos + :delta,
                       o.updatedAt = :now
                 where o.id = :orderId
                   and (o.usedPhotos + :delta) <= o.photosLimit
            """)
    int reservePhotos(@Param("orderId") UUID orderId,
                      @Param("delta") int delta,
                      @Param("now") Instant now);

    // =====================================================================
    // НОВОЕ: метки для "добивок" (update)
    // =====================================================================

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update OrderEntity o
                   set o.offerShownAt = :shownAt,
                       o.updatedAt = :now
                 where o.id = :orderId
                   and o.offerShownAt is null
            """)
    int setOfferShownAtIfNull(@Param("orderId") UUID orderId,
                              @Param("shownAt") Instant shownAt,
                              @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update OrderEntity o
                   set o.purchasedAt = :purchasedAt,
                       o.updatedAt = :now
                 where o.id = :orderId
            """)
    int setPurchasedAt(@Param("orderId") UUID orderId,
                       @Param("purchasedAt") Instant purchasedAt,
                       @Param("now") Instant now);

    // ✅ ИДЕМПОТЕНТНО: ставим только если null (антидубли шедулера)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update OrderEntity o
                   set o.followup30SentAt = :sentAt,
                       o.updatedAt = :now
                 where o.id = :orderId
                   and o.followup30SentAt is null
            """)
    int setFollowup30SentAtIfNull(@Param("orderId") UUID orderId,
                                  @Param("sentAt") Instant sentAt,
                                  @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update OrderEntity o
                   set o.followup24SentAt = :sentAt,
                       o.updatedAt = :now
                 where o.id = :orderId
                   and o.followup24SentAt is null
            """)
    int setFollowup24SentAtIfNull(@Param("orderId") UUID orderId,
                                  @Param("sentAt") Instant sentAt,
                                  @Param("now") Instant now);

    // =====================================================================
    // НОВОЕ: выборки кандидатов (select)
    // =====================================================================

    @Query("""
                select o
                  from OrderEntity o
                 where o.offerShownAt is not null
                   and o.purchasedAt is null
                   and o.followup30SentAt is null
                   and o.offerShownAt <= :deadline
            """)
    List<OrderEntity> findForFollowup30(@Param("deadline") Instant deadline);

    @Query("""
                select o
                  from OrderEntity o
                 where o.offerShownAt is not null
                   and o.purchasedAt is null
                   and o.followup24SentAt is null
                   and o.offerShownAt <= :deadline
            """)
    List<OrderEntity> findForFollowup24(@Param("deadline") Instant deadline);

    // =====================================================================
    // НОВОЕ: проекция (orderId + telegramId) — удобно для шедулера
    // =====================================================================

    interface FollowupRow {
        UUID getOrderId();

        Long getTelegramId();
    }

    @Query("""
                select o.id as orderId, u.telegramId as telegramId
                  from OrderEntity o
                  join o.user u
                 where o.offerShownAt is not null
                   and o.purchasedAt is null
                   and o.followup30SentAt is null
                   and o.offerShownAt <= :deadline
            """)
    List<FollowupRow> findForFollowup30Rows(@Param("deadline") Instant deadline);

    @Query("""
                select o.id as orderId, u.telegramId as telegramId
                  from OrderEntity o
                  join o.user u
                 where o.offerShownAt is not null
                   and o.offerShownAt <= :deadline
                   and o.purchasedAt is null
                   and o.followup30SentAt is not null
                   and o.followup24SentAt is null
            """)
    List<FollowupRow> findForFollowup24Rows(@Param("deadline") Instant deadline);
}