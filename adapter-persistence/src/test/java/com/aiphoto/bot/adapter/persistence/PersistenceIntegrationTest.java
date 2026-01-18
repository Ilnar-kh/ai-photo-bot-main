//package com.aiphoto.bot.adapter.persistence;
//
//import com.aiphoto.bot.adapter.persistence.repository.JobRepositoryAdapter;
//import com.aiphoto.bot.adapter.persistence.repository.OrderRepositoryAdapter;
//import com.aiphoto.bot.adapter.persistence.repository.OutboxRepositoryAdapter;
//import com.aiphoto.bot.adapter.persistence.repository.PresetRepositoryAdapter;
//import com.aiphoto.bot.adapter.persistence.repository.UploadRepositoryAdapter;
//import com.aiphoto.bot.adapter.persistence.repository.UserRepositoryAdapter;
//import com.aiphoto.bot.core.domain.*;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.time.Instant;
//import java.util.Map;
//import java.util.UUID;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@Testcontainers
//@ContextConfiguration(classes = PersistenceIntegrationTest.TestConfig.class)
//class PersistenceIntegrationTest {
//
//    // ---- Testcontainers ----
//    @Container
//    static final PostgreSQLContainer<?> POSTGRES =
//            new PostgreSQLContainer<>("postgres:16.3-alpine");
//
//    @DynamicPropertySource
//    static void props(DynamicPropertyRegistry r) {
//        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
//        r.add("spring.datasource.username", POSTGRES::getUsername);
//        r.add("spring.datasource.password", POSTGRES::getPassword);
//        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
//        r.add("spring.flyway.enabled", () -> "true");
//    }
//
//    // ---- Автовайрим адаптеры (они @Repository) ----
//    @org.springframework.beans.factory.annotation.Autowired UserRepositoryAdapter userRepository;
//    @org.springframework.beans.factory.annotation.Autowired PresetRepositoryAdapter presetRepository;
//    @org.springframework.beans.factory.annotation.Autowired OrderRepositoryAdapter orderRepository;
//    @org.springframework.beans.factory.annotation.Autowired UploadRepositoryAdapter uploadRepository;
//    @org.springframework.beans.factory.annotation.Autowired JobRepositoryAdapter jobRepository;
//    @org.springframework.beans.factory.annotation.Autowired OutboxRepositoryAdapter outboxRepository;
//
//    @Test
//    void persistsOrderLifecycle() {
//        Instant now = Instant.parse("2024-01-01T10:15:30Z");
//
//        User user = userRepository.save(new User(UUID.randomUUID(), 9999L, "persisted", now));
//        Preset preset = presetRepository.findByName("Studio Portrait").orElseThrow();
//
//        Order order = orderRepository.save(new Order(UUID.randomUUID(), user.id(), preset.id(), OrderStatus.NEW, now, now));
//        Upload upload = uploadRepository.save(new Upload(UUID.randomUUID(), order.id(), "key", "image/png", now));
//        Order uploading = orderRepository.save(order.withStatus(OrderStatus.UPLOADING, now));
//        Job job = jobRepository.save(new Job(UUID.randomUUID(), order.id(), "ext-job", now, now));
//        OutboxEvent savedEvent = outboxRepository.save(new OutboxEvent(
//                UUID.randomUUID(), "Order", order.id(), Map.of("type", "Test"), now, now));
//
//        Assertions.assertThat(uploading.status()).isEqualTo(OrderStatus.UPLOADING);
//        Assertions.assertThat(upload.objectKey()).isEqualTo("key");
//        Assertions.assertThat(uploadRepository.findByOrderId(order.id())).hasSize(1);
//        Assertions.assertThat(jobRepository.findByExternalId("ext-job")).contains(job);
//        Assertions.assertThat(savedEvent.payload()).containsEntry("type", "Test");
//    }
//
//    // ---- Мини-конфиг только для теста ----
//    @TestConfiguration
//    @Configuration
//    @EntityScan(basePackages = "com.aiphoto.bot.adapter.persistence.entity")
//    @EnableJpaRepositories(basePackages = "com.aiphoto.bot.adapter.persistence.repository.jpa")
//    @ComponentScan(basePackages = "com.aiphoto.bot.adapter.persistence")
//    static class TestConfig { }
//}