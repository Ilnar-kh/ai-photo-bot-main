package com.aiphoto.bot.adapter.web.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

@Configuration
@ComponentScan(basePackages = "com.aiphoto.bot.adapter.web")
public class WebAdapterConfig {

    @Bean
    @Qualifier("minioInternal")
    @Primary// для upload из Docker-сети
    public MinioClient minioInternal(
            @Value("${minio.endpoint:http://minio:9000}") String endpoint,
            @Value("${minio.access-key:minio}") String accessKey,
            @Value("${minio.secret-key:minio123}") String secretKey,
            @Value("${minio.region:}") String region
    ) {
        MinioClient.Builder b = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey);
        if (StringUtils.hasText(region)) {
            b.region(region.trim());
        }
        return b.build();
    }

    @Bean
    @Qualifier("minioPublic") // для presign на внешний хост
    public MinioClient minioPublic(
            @Value("${minio.public-endpoint:http://89.169.187.165:9000}") String publicEndpoint,
            @Value("${minio.access-key:minio}") String accessKey,
            @Value("${minio.secret-key:minio123}") String secretKey,
            @Value("${minio.region:}") String region
    ) {
        MinioClient.Builder b = MinioClient.builder()
                .endpoint(publicEndpoint)
                .credentials(accessKey, secretKey);
        if (StringUtils.hasText(region)) {
            b.region(region.trim());
        }
        return b.build();
    }

    // создаём бакет при старте через ВНУТРЕННИЙ клиент
    @Bean
    public ApplicationRunner ensureBucket(
            @Qualifier("minioInternal") MinioClient minio,
            @Value("${minio.bucket:ai-photo-bot}") String bucket
    ) {
        return args -> {
            boolean exists = minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        };
    }
}