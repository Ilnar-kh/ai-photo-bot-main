package com.aiphoto.bot.app;

import com.aiphoto.bot.adapter.ml.config.MlAdapterConfig;
import com.aiphoto.bot.adapter.persistence.PersistenceConfig;
import com.aiphoto.bot.adapter.telegram.config.TelegramAdapterConfig;
import com.aiphoto.bot.adapter.web.config.WebAdapterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import({PersistenceConfig.class, WebAdapterConfig.class, TelegramAdapterConfig.class, MlAdapterConfig.class})
public class AiPhotoBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiPhotoBotApplication.class, args);
    }
}
