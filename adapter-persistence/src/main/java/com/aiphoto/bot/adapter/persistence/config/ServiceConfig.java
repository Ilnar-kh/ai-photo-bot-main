package com.aiphoto.bot.adapter.persistence.config;

import com.aiphoto.bot.core.port.external.TelegramClient;
import com.aiphoto.bot.core.port.persistence.OrderRepository;
import com.aiphoto.bot.core.port.persistence.PresetRepository;
import com.aiphoto.bot.core.port.persistence.UploadRepository;
import com.aiphoto.bot.core.port.persistence.UserRepository;
import com.aiphoto.bot.core.service.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public OrderService orderService(
            UserRepository users,
            PresetRepository presets,
            OrderRepository orders,
            UploadRepository uploads,
            TelegramClient telegramClient
    ) {
        return new OrderService(users, presets, orders, uploads, telegramClient);
    }
}
