package com.aiphoto.bot.adapter.ml.config;

import com.aiphoto.bot.adapter.ml.FalLoraTrainerClient;
import com.aiphoto.bot.adapter.ml.FalPhotoGenClient;
import com.aiphoto.bot.core.port.external.LoraTrainerClient;
import com.aiphoto.bot.core.port.external.PhotoGenClient;
import com.aiphoto.bot.core.port.persistence.OrderRepository;
import com.aiphoto.bot.core.service.LoraTrainerService;
import com.aiphoto.bot.core.service.PhotoGenService;
import com.aiphoto.bot.core.service.UploadService;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(FalClientProperties.class)
public class MlAdapterConfig {

    @Bean
    public WebClient falWebClient(FalClientProperties properties, WebClient.Builder builder) {
        WebClient.Builder configured = builder.clone()
                .baseUrl(properties.getApiBase())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                        .build());
        if (StringUtils.hasText(properties.getApiKey())) {
            configured.defaultHeader(HttpHeaders.AUTHORIZATION, "Key " + properties.getApiKey());
        }
        return configured.build();
    }

    @Bean
    public WebClient falDownloadWebClient(WebClient.Builder builder) {
        return builder.clone()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                        .build())
                .build();
    }

    @Bean
    public PhotoGenClient photoGenClient(WebClient falWebClient,
                                         WebClient falDownloadWebClient,
                                         FalClientProperties properties) {
        return new FalPhotoGenClient(falWebClient, falDownloadWebClient, properties);
    }

    @Bean
    public PhotoGenService photoGenService(PhotoGenClient photoGenClient, FalClientProperties properties) {
        FalClientProperties.Generation generation = properties.getGeneration();
        FalClientProperties.Realism realism = properties.getRealism();
        return new PhotoGenService(photoGenClient,
                generation.getWidth(),
                generation.getHeight(),
                generation.getSteps(),
                generation.getGuidance(),
                generation.getLoraScale(),
                generation.getSeed(),
                new PhotoGenClient.RealismParams(realism.getSteps(), realism.getStrength()),
                properties.getBusiness().getImagesPerRequest());
    }

    @Bean
    public LoraTrainerClient loraTrainerClient(WebClient falWebClient, FalClientProperties properties) {
        return new FalLoraTrainerClient(falWebClient, properties);
    }

    @Bean
    public LoraTrainerService loraTrainerService(
            OrderRepository orderRepository,
            UploadService uploadService,
            LoraTrainerClient loraTrainerClient,
            @Qualifier("minioInternal") MinioClient minioInternal,
            @Qualifier("minioPublic") MinioClient minioPublic,
            @Value("${minio.bucket:ai-photo-bot}") String bucket,
            Clock clock,
            FalClientProperties properties
    ) {
        return new LoraTrainerService(
                orderRepository,
                uploadService,
                loraTrainerClient,
                minioInternal,    // внутренний MinIO (docker:9000)
                minioPublic,      // внешний MinIO (158.160.19.160:9000)
                bucket,
                clock,
                properties.getBusiness().getMinPhotos(),
                properties.getBusiness().getMaxPhotos(),
                properties.getTraining().getSteps()
        );
    }
}