package com.aiphoto.bot.core.service;

import com.aiphoto.bot.core.domain.Order;
import com.aiphoto.bot.core.domain.OrderStatus;
import com.aiphoto.bot.core.domain.OutboxEvent;
import com.aiphoto.bot.core.domain.Preset;
import com.aiphoto.bot.core.domain.User;
import com.aiphoto.bot.core.port.persistence.OrderRepository;
import com.aiphoto.bot.core.port.persistence.OutboxRepository;
import com.aiphoto.bot.core.port.persistence.PresetRepository;
import com.aiphoto.bot.core.port.persistence.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CreateOrderService {

    private final UserRepository userRepository;
    private final PresetRepository presetRepository;
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final Clock clock;

    public CreateOrderService(UserRepository userRepository,
                              PresetRepository presetRepository,
                              OrderRepository orderRepository,
                              OutboxRepository outboxRepository,
                              Clock clock) {
        this.userRepository = userRepository;
        this.presetRepository = presetRepository;
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.clock = clock;
    }

    public record Command(long telegramId, String username, UUID presetId) {}

    public record Result(Order order, User user, boolean userCreated) {}

    public Result createOrder(Command command) {
        Preset preset = presetRepository.findById(command.presetId())
            .orElseThrow(() -> new IllegalArgumentException("Preset not found: " + command.presetId()));

        Instant now = clock.instant();
        Optional<User> existingUser = userRepository.findByTelegramId(command.telegramId());
        boolean userCreated = existingUser.isEmpty();
        User user = existingUser
            .map(found -> updateUsernameIfRequired(found, command.username()))
            .orElseGet(() -> createUser(command.telegramId(), command.username(), now));

        Order order = new Order(UUID.randomUUID(), user.id(), preset.id(), OrderStatus.NEW, now, now, null, null);
        Order persisted = orderRepository.save(order);

        OutboxEvent event = new OutboxEvent(
            UUID.randomUUID(),
            "Order",
            persisted.id(),
            Map.of(
                "type", "OrderCreated",
                "orderId", persisted.id().toString(),
                "userId", user.id().toString(),
                "presetId", preset.id().toString()
            ),
            now,
            now
        );
        outboxRepository.save(event);
        return new Result(persisted, user, userCreated);
    }

    private User updateUsernameIfRequired(User user, String username) {
        if (username != null && !username.equals(user.username())) {
            User updated = user.withUsername(username);
            return userRepository.save(updated);
        }
        return user;
    }

    private User createUser(long telegramId, String username, Instant now) {
        User user = new User(UUID.randomUUID(), telegramId, username, now);
        return userRepository.save(user);
    }
}
