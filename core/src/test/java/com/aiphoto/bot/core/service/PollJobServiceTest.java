//package com.aiphoto.bot.core.service;
//
//import com.aiphoto.bot.core.domain.Image;
//import com.aiphoto.bot.core.domain.Job;
//import com.aiphoto.bot.core.domain.Order;
//import com.aiphoto.bot.core.domain.OrderStatus;
//import com.aiphoto.bot.core.domain.OutboxEvent;
//import com.aiphoto.bot.core.port.external.ImageGenClient;
//import com.aiphoto.bot.core.port.persistence.ImageRepository;
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
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//
//class PollJobServiceTest {
//
//    private InMemoryJobRepository jobRepository;
//    private InMemoryOrderRepository orderRepository;
//    private InMemoryImageRepository imageRepository;
//    private InMemoryOutboxRepository outboxRepository;
//    private StubImageGenClient imageGenClient;
//    private PollJobService service;
//    private final Instant fixedInstant = Instant.parse("2024-01-01T10:15:30Z");
//
//    @BeforeEach
//    void setUp() {
//        jobRepository = new InMemoryJobRepository();
//        orderRepository = new InMemoryOrderRepository();
//        imageRepository = new InMemoryImageRepository();
//        outboxRepository = new InMemoryOutboxRepository();
//        imageGenClient = new StubImageGenClient();
//        service = new PollJobService(imageGenClient, jobRepository, orderRepository, imageRepository, outboxRepository,
//            Clock.fixed(fixedInstant, ZoneOffset.UTC));
//    }
//
//    @Test
//    void storesImagesWhenJobDone() {
//        UUID orderId = UUID.randomUUID();
//        UUID jobId = UUID.randomUUID();
//        Order order = new Order(orderId, UUID.randomUUID(), UUID.randomUUID(), OrderStatus.PROCESSING, fixedInstant, fixedInstant, null, null);
//        Job job = new Job(jobId, orderId, "ext-1", fixedInstant, fixedInstant);
//        orderRepository.save(order);
//        jobRepository.save(job);
//        imageGenClient.nextStatus = new ImageGenClient.JobStatus("ext-1", ImageGenClient.Status.DONE,
//            List.of("img-1", "img-2"), null);
//
//        PollJobService.Result result = service.pollJob(new PollJobService.Command("ext-1"));
//
//        Assertions.assertThat(result.images()).hasSize(2);
//        Assertions.assertThat(imageRepository.images).hasSize(2);
//        Assertions.assertThat(result.order().status()).isEqualTo(OrderStatus.DONE);
//        Assertions.assertThat(outboxRepository.lastEvent.aggregateId()).isEqualTo(orderId);
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
//    private static class InMemoryImageRepository implements ImageRepository {
//        private final List<Image> images = new ArrayList<>();
//
//        @Override
//        public Image save(Image image) {
//            images.add(image);
//            return image;
//        }
//
//        @Override
//        public List<Image> findByJobId(UUID jobId) {
//            return images.stream().filter(i -> i.jobId().equals(jobId)).toList();
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
//
//    private static class StubImageGenClient implements ImageGenClient {
//        private JobStatus nextStatus;
//
//        @Override
//        public JobStatus pollJob(String externalId) {
//            if (nextStatus == null) {
//                return new JobStatus(externalId, Status.QUEUED, null, null);
//            }
//            return nextStatus;
//        }
//    }
//}
