package com.aiphoto.bot.core.service;

import com.aiphoto.bot.core.domain.Order;
import com.aiphoto.bot.core.domain.OrderStatus;
import com.aiphoto.bot.core.domain.OutboxEvent;
import com.aiphoto.bot.core.domain.Upload;
import com.aiphoto.bot.core.port.persistence.OrderRepository;
import com.aiphoto.bot.core.port.persistence.OutboxRepository;
import com.aiphoto.bot.core.port.persistence.UploadRepository;
import com.aiphoto.bot.core.exceptions.UploadLimitExceededException; // 👈 добавить import

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AttachUploadService {

    private final OrderRepository orderRepository;
    private final UploadRepository uploadRepository;
    private final OutboxRepository outboxRepository;
    private final Clock clock;

    // 👇 добавляем константу лимита
    private static final int MAX_GENERATED_PHOTOS = 60;

    public AttachUploadService(OrderRepository orderRepository,
                               UploadRepository uploadRepository,
                               OutboxRepository outboxRepository,
                               Clock clock) {
        this.orderRepository = orderRepository;
        this.uploadRepository = uploadRepository;
        this.outboxRepository = outboxRepository;
        this.clock = clock;
    }

    public record Command(UUID orderId, String objectKey, String contentType) {}

    public record Result(Order order, Upload upload) {}

    public Result attachUpload(Command command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        if (order.status() == OrderStatus.CANCELED || order.status() == OrderStatus.DONE || order.status() == OrderStatus.FAILED) {
            throw new IllegalStateException("Cannot attach upload to order in status " + order.status());
        }

        // 👇 ПРОВЕРКА ЛИМИТА ПЕРЕД СОХРАНЕНИЕМ
        int currentCount = uploadRepository.findByOrderId(order.id()).size();
        if (currentCount >= MAX_GENERATED_PHOTOS) {
            throw new UploadLimitExceededException(MAX_GENERATED_PHOTOS, currentCount);
        }

        Instant now = clock.instant();
        Upload upload = new Upload(UUID.randomUUID(), order.id(), command.objectKey(), command.contentType(), now);
        Upload savedUpload = uploadRepository.save(upload);
        Order updatedOrder = orderRepository.save(order.withStatus(OrderStatus.UPLOADING, now));

        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "Order",
                updatedOrder.id(),
                Map.of(
                        "type", "UploadAttached",
                        "orderId", updatedOrder.id().toString(),
                        "objectKey", savedUpload.objectKey()
                ),
                now,
                now
        );
        outboxRepository.save(event);

        return new Result(updatedOrder, savedUpload);
    }
}