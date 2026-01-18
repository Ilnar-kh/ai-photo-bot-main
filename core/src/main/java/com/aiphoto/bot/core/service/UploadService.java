package com.aiphoto.bot.core.service;

import com.aiphoto.bot.core.domain.Upload;
import com.aiphoto.bot.core.exceptions.UploadLimitExceededException;
import com.aiphoto.bot.core.port.persistence.UploadRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UploadService {

    private static final int DEFAULT_UPLOAD_LIMIT = 60;

    private final UploadRepository uploadRepository;

    public UploadService(UploadRepository uploadRepository) {
        this.uploadRepository = uploadRepository;
    }

    public List<Upload> listUploads(UUID orderId) {
        return uploadRepository.findByOrderId(orderId);
    }

    public int countUploads(UUID orderId) {
        return uploadRepository.findByOrderId(orderId).size();
    }

    public List<String> listImageUrls(UUID orderId) {
        return uploadRepository.findByOrderId(orderId).stream()
                .map(Upload::objectKey)
                .toList();
    }

    // === НОВОЕ: сохраняем прямой URL Telegram как objectKey ===
    public void saveUrl(UUID orderId, String url) {
        ensureUnderLimit(orderId); // страховка

        Upload u = new Upload(
                UUID.randomUUID(),
                orderId,
                url,
                "image/jpeg",
                Instant.now()
        );

        uploadRepository.save(u);
    }

    public void ensureUnderLimit(UUID orderId) {
        int current = countUploads(orderId);
        if (current >= DEFAULT_UPLOAD_LIMIT) {
            throw new UploadLimitExceededException(DEFAULT_UPLOAD_LIMIT, current);
        }
    }
}