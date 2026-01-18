package com.aiphoto.bot.core.service;

import com.aiphoto.bot.core.domain.Job;
import com.aiphoto.bot.core.domain.Order;
import com.aiphoto.bot.core.domain.OrderStatus;
import com.aiphoto.bot.core.domain.OutboxEvent;
import com.aiphoto.bot.core.port.persistence.JobRepository;
import com.aiphoto.bot.core.port.persistence.OrderRepository;
import com.aiphoto.bot.core.port.persistence.OutboxRepository;
import com.aiphoto.bot.core.port.persistence.UploadRepository; // 👈 новый import
import com.aiphoto.bot.core.exceptions.UploadLimitExceededException; // 👈 новый import

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class EnqueueJobService {

    private final OrderRepository orderRepository;
    private final JobRepository jobRepository;
    private final OutboxRepository outboxRepository;
    private final UploadRepository uploadRepository; // 👈 новое поле
    private final Clock clock;

    // 👇 константа лимита (можно вынести в одно место, если хочешь)
    private static final int MAX_GENERATED_PHOTOS = 60;

    public EnqueueJobService(OrderRepository orderRepository,
                             JobRepository jobRepository,
                             OutboxRepository outboxRepository,
                             UploadRepository uploadRepository,   // 👈 реально добавили параметр
                             Clock clock) {
        this.orderRepository = orderRepository;
        this.jobRepository = jobRepository;
        this.outboxRepository = outboxRepository;
        this.uploadRepository = uploadRepository; // 👈 теперь это аргумент конструктора
        this.clock = clock;
    }

    public record Command(UUID orderId, String externalId) {}

    public record Result(Order order, Job job) {}

    public Result enqueueJob(Command command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        jobRepository.findByOrderId(order.id()).ifPresent(existing -> {
            throw new IllegalStateException("Job already exists for order " + order.id());
        });

        if (order.status() == OrderStatus.CANCELED || order.status() == OrderStatus.DONE || order.status() == OrderStatus.FAILED) {
            throw new IllegalStateException("Cannot enqueue job for order in status " + order.status());
        }

        // 👇 ГЛАВНАЯ ПРОВЕРКА ЛИМИТА НА ГЕНЕРАЦИЮ
        int currentCount = uploadRepository.findByOrderId(order.id()).size();
        if (currentCount >= MAX_GENERATED_PHOTOS) {
            throw new UploadLimitExceededException(MAX_GENERATED_PHOTOS, currentCount);
        }

        Instant now = clock.instant();
        Job job = new Job(UUID.randomUUID(), order.id(), command.externalId(), now, now);
        Job savedJob = jobRepository.save(job);
        Order updatedOrder = orderRepository.save(order.withStatus(OrderStatus.QUEUED, now));

        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "Order",
                updatedOrder.id(),
                Map.of(
                        "type", "JobQueued",
                        "orderId", updatedOrder.id().toString(),
                        "externalId", savedJob.externalId()
                ),
                now,
                now
        );
        outboxRepository.save(event);

        return new Result(updatedOrder, savedJob);
    }
}