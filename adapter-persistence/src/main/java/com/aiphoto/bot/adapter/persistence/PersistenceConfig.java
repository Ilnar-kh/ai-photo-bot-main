package com.aiphoto.bot.adapter.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.aiphoto.bot.adapter.persistence.entity")
@EnableJpaRepositories(basePackages = "com.aiphoto.bot.adapter.persistence.repository.jpa")
@ComponentScan(basePackages = {"com.aiphoto.bot.adapter.persistence.repository", "com.aiphoto.bot.adapter.persistence.config"})
public class PersistenceConfig {
}
