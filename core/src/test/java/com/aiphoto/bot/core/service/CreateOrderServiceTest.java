//package com.aiphoto.bot.core.service;
//
//import com.aiphoto.bot.core.domain.Order;
//import com.aiphoto.bot.core.domain.OrderStatus;
//import com.aiphoto.bot.core.domain.OutboxEvent;
//import com.aiphoto.bot.core.domain.Preset;
//import com.aiphoto.bot.core.domain.User;
//import com.aiphoto.bot.core.port.persistence.OrderRepository;
//import com.aiphoto.bot.core.port.persistence.OutboxRepository;
//import com.aiphoto.bot.core.port.persistence.PresetRepository;
//import com.aiphoto.bot.core.port.persistence.UserRepository;
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
//class CreateOrderServiceTest {
//
//    private InMemoryUserRepository userRepository;
//    private InMemoryPresetRepository presetRepository;
//    private InMemoryOrderRepository orderRepository;
//    private InMemoryOutboxRepository outboxRepository;
//    private CreateOrderService service;
//    private final Instant fixedInstant = Instant.parse("2024-01-01T10:15:30Z");
//
//    @BeforeEach
//    void setUp() {
//        userRepository = new InMemoryUserRepository();
//        presetRepository = new InMemoryPresetRepository();
//        orderRepository = new InMemoryOrderRepository();
//        outboxRepository = new InMemoryOutboxRepository();
//        service = new CreateOrderService(userRepository, presetRepository, orderRepository, outboxRepository,
//            Clock.fixed(fixedInstant, ZoneOffset.UTC));
//    }
//
//    @Test
//    void createsOrderAndUser() {
//        UUID presetId = UUID.randomUUID();
//        presetRepository.save(new Preset(presetId, "Studio Portrait", "model-a", Map.of(), fixedInstant));
//
//        CreateOrderService.Result result = service.createOrder(new CreateOrderService.Command(123L, "alice", presetId));
//
//        Assertions.assertThat(result.userCreated()).isTrue();
//        Assertions.assertThat(result.order().status()).isEqualTo(OrderStatus.NEW);
//        Assertions.assertThat(orderRepository.findById(result.order().id())).isPresent();
//        Assertions.assertThat(outboxRepository.lastEvent.aggregateId()).isEqualTo(result.order().id());
//    }
//
//    private static class InMemoryUserRepository implements UserRepository {
//        private final Map<UUID, User> storage = new HashMap<>();
//
//        @Override
//        public Optional<User> findByTelegramId(long telegramId) {
//            return storage.values().stream().filter(u -> u.telegramId() == telegramId).findFirst();
//        }
//
//        @Override
//        public User save(User user) {
//            storage.put(user.id(), user);
//            return user;
//        }
//
//        @Override
//        public Optional<User> findById(UUID id) {
//            return Optional.ofNullable(storage.get(id));
//        }
//    }
//
//    private static class InMemoryPresetRepository implements PresetRepository {
//        private final Map<UUID, Preset> storage = new HashMap<>();
//
//        @Override
//        public Optional<Preset> findById(UUID id) {
//            return Optional.ofNullable(storage.get(id));
//        }
//
//        @Override
//        public Optional<Preset> findByName(String name) {
//            return storage.values().stream().filter(p -> p.name().equals(name)).findFirst();
//        }
//
//        @Override
//        public java.util.List<Preset> findAll() {
//            return java.util.List.copyOf(storage.values());
//        }
//
//        public Preset save(Preset preset) {
//            storage.put(preset.id(), preset);
//            return preset;
//        }
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
//    private static class InMemoryOutboxRepository implements OutboxRepository {
//        private OutboxEvent lastEvent;
//
//        @Override
//        public OutboxEvent save(OutboxEvent event) {
//            this.lastEvent = event;
//            return event;
//        }
//    }
//}
