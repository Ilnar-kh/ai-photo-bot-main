package com.aiphoto.bot.core.service;

import com.aiphoto.bot.core.domain.Gender;
import com.aiphoto.bot.core.domain.LoraStatus;
import com.aiphoto.bot.core.domain.Order;
import com.aiphoto.bot.core.port.external.LoraTrainerClient;
import com.aiphoto.bot.core.port.persistence.OrderRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LoraTrainerService {

    private final OrderRepository orderRepository;
    private final UploadService uploadService;
    private final LoraTrainerClient loraTrainerClient;

    // ВНУТРЕННИЙ клиент — запись в MinIO по docker-имени
    private final MinioClient minioInternal;
    // ПУБЛИЧНЫЙ клиент — подпись на внешнем хосте
    private final MinioClient minioPublic;

    private final String bucket;
    private final Clock clock;
    private final int minPhotos;
    private final int maxPhotos;
    private final int trainingSteps;

    public LoraTrainerService(OrderRepository orderRepository,
                              UploadService uploadService,
                              LoraTrainerClient loraTrainerClient,
                              MinioClient minioInternal,   // <-- внутренний
                              MinioClient minioPublic,     // <-- внешний
                              String bucket,
                              Clock clock,
                              int minPhotos,
                              int maxPhotos,
                              int trainingSteps) {
        this.orderRepository = orderRepository;
        this.uploadService = uploadService;
        this.loraTrainerClient = loraTrainerClient;
        this.minioInternal = minioInternal;
        this.minioPublic = minioPublic;
        this.bucket = bucket;
        this.clock = clock;
        this.minPhotos = minPhotos;
        this.maxPhotos = maxPhotos;
        this.trainingSteps = trainingSteps;
    }

    /**
     * Запустить обучение LoRA и сохранить результат в заказ.
     * Работает синхронно относительно FAL: train() возвращает готовые веса.
     */
    public Mono<LoraTrainerClient.TrainingResult> trainAndPersist(UUID orderId) {
        // 1. Достаём заказ
        return Mono.fromCallable(() ->
                        orderRepository.findById(orderId)
                                .orElseThrow(() ->
                                        new IllegalArgumentException("Order %s not found".formatted(orderId)))
                )
                // 2. Проверяем статус, пол, кол-во фото и собираем TrainingRequest
                .flatMap(order -> {
                    if (order.loraStatus() == LoraStatus.SUBMITTED) {
                        return Mono.error(new IllegalStateException("LoRA training already in progress"));
                    }
                    if (order.loraStatus() == LoraStatus.READY) {
                        return Mono.error(new IllegalStateException("LoRA is already trained"));
                    }
                    if (order.gender() == null) {
                        return Mono.error(new IllegalStateException("Gender must be set before training"));
                    }

                    List<String> images = uploadService.listImageUrls(orderId);
                    int count = images.size();
                    if (count < minPhotos) {
                        return Mono.error(new IllegalStateException("Need at least %d photos".formatted(minPhotos)));
                    }
                    if (count > maxPhotos) {
                        return Mono.error(new IllegalStateException("At most %d photos allowed".formatted(maxPhotos)));
                    }

                    // zip + presigned url
                    String presignedZipUrl = createPresignedZipFromUrls(orderId, images);

                    LoraTrainerClient.TrainingRequest request = new LoraTrainerClient.TrainingRequest(
                            presignedZipUrl,
                            buildTriggerPhrase(order),
                            trainingSteps
                    );

                    // 3. Отправляем в FAL и по завершению сохраняем результат в заказ
                    return loraTrainerClient.train(request)
                            .flatMap(result -> Mono.fromCallable(() -> {
                                Instant now = clock.instant();
                                orderRepository.save(order.withLoraReady(
                                        result.loraPath(),
                                        result.configUrl(),
                                        now,
                                        now
                                ));
                                return result;
                            }));
                });
    }

    private String buildTriggerPhrase(Order order) {
        Gender gender = order.gender();
        if (gender == null) return null;
        return "LORA_" + order.id();
    }

    // ===== ZIP + upload + presign =====
    private String createPresignedZipFromUrls(UUID orderId, List<String> urls) {
        try {
            String zipObject = "training/" + orderId + ".zip";

            // 1) ZIP во временный файл
            File tmp = File.createTempFile(orderId.toString(), ".zip");
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmp))) {
                int i = 0;
                for (String url : urls) {
                    byte[] bytes = download(url);
                    if (bytes == null || bytes.length == 0) continue;
                    zos.putNextEntry(new ZipEntry("img_" + (++i) + ".jpg"));
                    zos.write(bytes);
                    zos.closeEntry();
                }
            }

            // 2) Upload во внутренний MinIO
            try (FileInputStream fis = new FileInputStream(tmp)) {
                minioInternal.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(zipObject)
                                .contentType("application/zip")
                                .stream(fis, tmp.length(), -1)
                                .build()
                );
            } finally {
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
            }

            // 3) Presign на внешнем endpoint — БЕЗ каких-либо replace
            return minioPublic.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(zipObject)
                            .method(Method.GET)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to build/publish training zip", e);
        }
    }

    private static byte[] download(String url) {
        try (InputStream in = new URL(url).openStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            in.transferTo(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}