//package com.aiphoto.bot.core.service;
//
//import com.aiphoto.bot.core.domain.Order;
//import com.aiphoto.bot.core.domain.OrderStatus;
//import com.aiphoto.bot.core.domain.OutboxEvent;
//import com.aiphoto.bot.core.domain.Upload;
//import com.aiphoto.bot.core.port.persistence.OrderRepository;
//import com.aiphoto.bot.core.port.persistence.OutboxRepository;
//import com.aiphoto.bot.core.port.persistence.UploadRepository;
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
//class AttachUploadServiceTest {
//
//    private final Instant fixedInstant = Instant.parse("2024-01-01T10:15:30Z");
//    private AttachUploadService service;
//    private InMemoryOrderRepository orderRepository;
//    private InMemoryUploadRepository uploadRepository;
//    private InMemoryOutboxRepository outboxRepository;
//
//    @BeforeEach
//    void setUp() {
//        orderRepository = new InMemoryOrderRepository();
//        uploadRepository = new InMemoryUploadRepository();
//        outboxRepository = new InMemoryOutboxRepository();
//        service = new AttachUploadService(orderRepository, uploadRepository, outboxRepository,
//            Clock.fixed(fixedInstant, ZoneOffset.UTC));
//    }
//
//    @Test
//    void attachesUploadAndUpdatesStatus() {
//        UUID orderId = UUID.randomUUID();
//        Order order = new Order(orderId, UUID.randomUUID(), UUID.randomUUID(), OrderStatus.NEW, fixedInstant, fixedInstant, null, null);
//        orderRepository.save(order);
//
//        AttachUploadService.Result result = service.attachUpload(
//            new AttachUploadService.Command(orderId, "uploads/file.png", "image/png"));
//
//        Assertions.assertThat(result.order().status()).isEqualTo(OrderStatus.UPLOADING);
//        Assertions.assertThat(uploadRepository.uploads).hasSize(1);
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
//    private static class InMemoryUploadRepository implements UploadRepository {
//        private final Map<UUID, Upload> uploads = new HashMap<>();
//
//        @Override
//        public Upload save(Upload upload) {
//            uploads.put(upload.id(), upload);
//            return upload;
//        }
//
//        @Override
//        public java.util.List<Upload> findByOrderId(UUID orderId) {
//            return uploads.values().stream().filter(u -> u.orderId().equals(orderId)).toList();
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
