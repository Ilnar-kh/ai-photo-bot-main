//package com.aiphoto.bot.core.service;
//
//import com.aiphoto.bot.core.domain.Job;
//import com.aiphoto.bot.core.domain.Order;
//import com.aiphoto.bot.core.domain.OrderStatus;
//import com.aiphoto.bot.core.domain.OutboxEvent;
//import com.aiphoto.bot.core.port.persistence.JobRepository;
//import com.aiphoto.bot.core.port.persistence.OrderRepository;
//import com.aiphoto.bot.core.port.persistence.OutboxRepository;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.time.Clock;
//import java.time.Instant;
//import java.time.ZoneOffset;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//
//class EnqueueJobServiceTest {
//
//    private EnqueueJobService service;
//    private InMemoryOrderRepository orderRepository;
//    private InMemoryJobRepository jobRepository;
//    private InMemoryOutboxRepository outboxRepository;
//    private final Instant fixedInstant = Instant.parse("2024-01-01T10:15:30Z");
//
//    @BeforeEach
//    void setUp() {
//        orderRepository = new InMemoryOrderRepository();
//        jobRepository = new InMemoryJobRepository();
//        outboxRepository = new InMemoryOutboxRepository();
//        service = new EnqueueJobService(orderRepository, jobRepository, outboxRepository,
//            Clock.fixed(fixedInstant, ZoneOffset.UTC));
//    }
//
//    @Test
//    void enqueuesJobAndPublishesEvent() {
//        UUID orderId = UUID.randomUUID();
//        orderRepository.save(new Order(orderId, UUID.randomUUID(), UUID.randomUUID(), OrderStatus.UPLOADING, fixedInstant, fixedInstant, null, null));
//
//        EnqueueJobService.Result result = service.enqueueJob(new EnqueueJobService.Command(orderId, "ext-123"));
//
//        Assertions.assertThat(result.order().status()).isEqualTo(OrderStatus.QUEUED);
//        Assertions.assertThat(jobRepository.findByOrderId(orderId)).isPresent();
//        Assertions.assertThat(outboxRepository.lastEvent.aggregateId()).isEqualTo(orderId);
//    }
//
//    private static class InMemoryOrderRepository implements OrderRepository {
//        private final Map<UUID, Order> storage = new HashMap<>();
//
//        @Override
//        public Optional<Order> findLatestByUserId(UUID userId) {
//            // можно просто вернуть пустой Optional, тесты на это не завязаны
//            return Optional.empty();
//        }
//
//        @Override
//        public Order save(Order order) {
//            storage.put(order.id(), order);
//            return order;
//        }
//
//        @Override
//        public Optional<Order> findById(UUID id) {
//            return Optional.ofNullable(storage.get(id));
//        }
//    }
//
//    private static class InMemoryJobRepository implements JobRepository {
//        private final Map<UUID, Job> storage = new HashMap<>();
//
//        @Override
//        public Job save(Job job) {
//            storage.put(job.id(), job);
//            return job;
//        }
//
//        @Override
//        public Optional<Job> findByOrderId(UUID orderId) {
//            return storage.values().stream().filter(j -> j.orderId().equals(orderId)).findFirst();
//        }
//
//        @Override
//        public Optional<Job> findByExternalId(String externalId) {
//            return storage.values().stream().filter(j -> j.externalId().equals(externalId)).findFirst();
//        }
//    }
//
//    private static class InMemoryOutboxRepository implements OutboxRepository {
//        private OutboxEvent lastEvent;
//
//        @Override
//        public OutboxEvent save(OutboxEvent event) {
//            lastEvent = event;
//            return event;
//        }
//    }
//}
