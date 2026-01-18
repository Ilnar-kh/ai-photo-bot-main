package com.aiphoto.bot.core.service;

import com.aiphoto.bot.core.domain.*;
import com.aiphoto.bot.core.port.external.TelegramClient;
import com.aiphoto.bot.core.port.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class OrderService {
    private final UserRepository users;
    private final PresetRepository presets;
    private final OrderRepository orders;
    private final UploadRepository uploads;
    private final TelegramClient telegramClient;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);


    private static final String DEFAULT_PRESET_NAME = "Studio Portrait";

    public OrderService(UserRepository users, PresetRepository presets, OrderRepository orders, UploadRepository uploads,
                        TelegramClient telegramClient) {
        this.users = users;
        this.presets = presets;
        this.orders = orders;
        this.uploads = uploads;
        this.telegramClient = telegramClient;
    }

    // ——— Исключения
    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String msg) {
            super(msg);
        }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String msg) {
            super(msg);
        }
    }

    // ——— Создание заказа
    public Order createOrder(long telegramId) {
        Instant now = Instant.now();
        User user = users.findByTelegramId(telegramId)
                .orElseGet(() -> users.save(new User(UUID.randomUUID(), telegramId, null, now)));

        Preset preset = presets.findByName(DEFAULT_PRESET_NAME)
                .orElseThrow(() -> new NotFoundException("Default preset '%s' not found".formatted(DEFAULT_PRESET_NAME)));

        return orders.save(new Order(UUID.randomUUID(), user.id(), preset.id(), OrderStatus.NEW, now, now, null, null));
    }

    // ——— Добавление загрузки + автопостановка в очередь после первой загрузки
    public Upload addUpload(UUID orderId, String objectKey, String contentType) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));

        if (order.status() != OrderStatus.PAID) {
            throw new ForbiddenException("Нельзя загружать файлы, пока заказ не оплачен");
        }

        Instant now = Instant.now();
        Upload saved = uploads.save(new Upload(UUID.randomUUID(), orderId, objectKey, contentType, now));
        List<Upload> uploadsForOrder = uploads.findByOrderId(orderId);

        if (uploadsForOrder.size() == 1) {
            tryQueueAfterUpload(orderId);
        }

        // ——— вот тут анти-спам на «Можно запускать обучение»
        int count = uploadsForOrder.size();
        if (count >= 10 && !order.readyHintSent()) {
            long telegramId = findTelegramIdByOrder(orderId);

            String message = "Фотографий достаточно! Можно запускать обучение.";
            Map<String, Object> extra = Map.of(
                    "reply_markup", Map.of(
                            "inline_keyboard", List.of(
                                    List.of(Map.of(
                                            "text", "Начать обучение",
                                            "callback_data", "TRAIN|" + orderId
                                    ))
                            )
                    )
            );

            telegramClient.sendMessage(telegramId, message, extra)
                    .doOnError(e -> log.warn("Failed to notify about training readiness for order {}", orderId, e))
                    .subscribe();

            orders.save(order.withReadyHintSent(true, Instant.now())); // ← помечаем, что отправили
        }

        return saved;
    }

    public Optional<Order> findLatestOrderForTelegramUser(long telegramId) {
        return users.findByTelegramId(telegramId)
                .flatMap(user -> orders.findLatestByUserId(user.id()));
    }

    public long findTelegramIdByOrder(UUID orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));

        User user = users.findById(order.userId())
                .orElseThrow(() -> new NotFoundException("User %s not found".formatted(order.userId())));
        return user.telegramId();
    }

    // ——— Список загрузок
    public List<Upload> listUploads(UUID orderId) {
        orders.findById(orderId).orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        return uploads.findByOrderId(orderId);
    }

    // ——— Пометка оплаты
    public Order markPaid(UUID orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));

        // ✅ Если заказ уже оплачен — просто возвращаем его без ошибок
        if (order.status() == OrderStatus.PAID) {
            return order;
        }

        // ❌ Если заказ в любом другом состоянии (например, IN_PROGRESS, READY и т.п.) — ошибка
        if (order.status() != OrderStatus.NEW) {
            throw new IllegalStateException("Order in wrong state for payment: " + order.status());
        }

        // 💳 Переводим заказ из NEW → PAID
        Order updated = new Order(
                order.id(),
                order.userId(),
                order.presetId(),
                OrderStatus.PAID,
                order.createdAt(),
                Instant.now(),
                order.identityPath(),
                order.loraPath()
        );

        return orders.save(updated);
    }

    public void attachIdentity(UUID orderId, String identityPath) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        if (identityPath == null || identityPath.isBlank()) {
            throw new IllegalArgumentException("identityPath must not be blank");
        }
        orders.save(order.withIdentityPath(identityPath, Instant.now()));
    }

    public void attachLora(UUID orderId, String loraPath) {
        markLoraReady(orderId, loraPath, null, Instant.now());
    }

    public void markLoraSubmitted(UUID orderId, String requestId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        orders.save(order.withTrainingSubmission(requestId, Instant.now()));
    }

    public void markLoraReady(UUID orderId, String loraPath, String configUrl, Instant trainedAt) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        if (loraPath == null || loraPath.isBlank()) {
            throw new IllegalArgumentException("loraPath must not be blank");
        }
        orders.save(order.withLoraReady(loraPath, configUrl, trainedAt == null ? Instant.now() : trainedAt, Instant.now()));
    }

    public void markLoraFailed(UUID orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        orders.save(order.withLoraFailed(Instant.now()));
    }

    public String findIdentityPath(UUID orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        String identityPath = order.identityPath();
        if (identityPath == null || identityPath.isBlank()) {
            throw new NotFoundException("Identity for order %s not found".formatted(orderId));
        }
        return identityPath;
    }

    public Optional<String> findLoraPath(UUID orderId) {
        return orders.findById(orderId)
                .map(Order::loraPath)
                .filter(path -> path != null && !path.isBlank());
    }

    public Optional<Gender> findGender(UUID orderId) {
        return orders.findById(orderId).map(Order::gender);
    }

    public void setGender(UUID orderId, Gender gender) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        orders.save(order.withGender(gender, Instant.now()));
    }

    public Optional<LoraStatus> findLoraStatus(UUID orderId) {
        return orders.findById(orderId).map(Order::loraStatus);
    }

    // ——— Автопереход после (первой) загрузки
    public Order tryQueueAfterUpload(UUID orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));

        // защита от странных вызовов
        if (order.status() == OrderStatus.NEW) {
            throw new ForbiddenException("Order is not paid yet");
        }
        if (order.status() == OrderStatus.CANCELED || order.status() == OrderStatus.DONE) {
            return order; // ничего не делаем
        }

        Instant now = Instant.now();
        if (order.status() == OrderStatus.PAID) {
            order = orders.save(order.withStatus(OrderStatus.UPLOADING, now));
        }
        if (order.status() == OrderStatus.UPLOADING) {
            order = orders.save(order.withStatus(OrderStatus.QUEUED, Instant.now()));
        }
        return order;
    }

    public Order markProcessing(UUID orderId) {
        Order o = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        if (o.status() != OrderStatus.QUEUED) {
            throw new ForbiddenException("Order is not in QUEUED state");
        }
        return orders.save(o.withStatus(OrderStatus.PROCESSING, Instant.now()));
    }

    public Order markDone(UUID orderId) {
        Order o = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        if (o.status() != OrderStatus.PROCESSING) {
            throw new ForbiddenException("Order is not in PROCESSING state");
        }
        return orders.save(o.withStatus(OrderStatus.DONE, Instant.now()));
    }

    public Optional<Order> findLatestReadyOrderForTelegramUser(long telegramId) {
        return users.findByTelegramId(telegramId)
                .flatMap(user -> orders.findLatestReadyByUserId(user.id())); // нужен метод в репозитории
    }

    public void purgeUserDataForRetrain(long telegramId, UUID orderIdToKeep) {
        // реализация будет в адаптере
        throw new UnsupportedOperationException();
    }

    // ================== ЛИМИТ ФОТО ==================

    /**
     * Сколько фото уже сгенерировано по заказу.
     */
    public int getUsedPhotos(UUID orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        return order.usedPhotos();
    }

    /**
     * Текущий лимит фото по заказу.
     */
    public int getPhotosLimit(UUID orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));
        return order.photosLimit();
    }

    public void incrementUsedPhotos(UUID orderId, int delta) {
        if (delta <= 0) return;

        log.info("INC usedPhotos: orderId={}, delta={}", orderId, delta);
        orders.incrementUsedPhotos(orderId, delta); // ✅ атомарный UPDATE в адаптере
    }

    public void increasePhotosLimit(UUID orderId, int delta) {
        if (delta <= 0) return;

        orders.increasePhotosLimit(orderId, delta); // ✅ атомарный UPDATE в адаптере
    }

    /**
     * Пригодится, если захочешь сценарий "обнулить счётчик, лимит оставить".
     */
    public void resetUsedPhotos(UUID orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order %s not found".formatted(orderId)));

        orders.save(order.withUsedPhotos(0, Instant.now()));
    }
}