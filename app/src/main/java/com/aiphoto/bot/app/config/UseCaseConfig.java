package com.aiphoto.bot.app.config;

import com.aiphoto.bot.core.port.persistence.JobRepository;
import com.aiphoto.bot.core.port.persistence.OrderRepository;
import com.aiphoto.bot.core.port.persistence.OutboxRepository;
import com.aiphoto.bot.core.port.persistence.PresetRepository;
import com.aiphoto.bot.core.port.persistence.UploadRepository;
import com.aiphoto.bot.core.port.persistence.UserRepository;
import com.aiphoto.bot.core.service.AttachUploadService;
import com.aiphoto.bot.core.service.CreateOrderService;
import com.aiphoto.bot.core.service.EnqueueJobService;
import com.aiphoto.bot.core.service.UploadService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class UseCaseConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    public CreateOrderService createOrderService(UserRepository userRepository,
                                                 PresetRepository presetRepository,
                                                 OrderRepository orderRepository,
                                                 OutboxRepository outboxRepository,
                                                 Clock clock) {
        return new CreateOrderService(userRepository, presetRepository, orderRepository, outboxRepository, clock);
    }

    @Bean
    public AttachUploadService attachUploadService(OrderRepository orderRepository,
                                                   UploadRepository uploadRepository,
                                                   OutboxRepository outboxRepository,
                                                   Clock clock) {
        return new AttachUploadService(orderRepository, uploadRepository, outboxRepository, clock);
    }

    @Bean
    public UploadService uploadService(UploadRepository uploadRepository) {
        return new UploadService(uploadRepository);
    }

    @Bean
    public EnqueueJobService enqueueJobService(OrderRepository orderRepository,
                                               JobRepository jobRepository,
                                               OutboxRepository outboxRepository,
                                               UploadRepository uploadRepository,
                                               Clock clock) {
        return new EnqueueJobService(orderRepository, jobRepository, outboxRepository, uploadRepository, clock);
    }
}
