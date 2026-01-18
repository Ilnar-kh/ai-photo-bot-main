package com.aiphoto.bot.adapter.web.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class StorageService {
    private final MinioClient client;
    private final String bucket;

    public StorageService(MinioClient client, @Value("${minio.bucket:ai-photo-bot}") String bucket) {
        this.client = client; this.bucket = bucket;
    }

    public void put(String objectKey, String contentType, InputStream in, long size) throws Exception {
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .contentType(contentType)
                        .stream(in, size, -1)
                        .build()
        );
    }
}
