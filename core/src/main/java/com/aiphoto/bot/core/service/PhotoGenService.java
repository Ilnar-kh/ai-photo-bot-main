package com.aiphoto.bot.core.service;

import com.aiphoto.bot.core.port.external.PhotoGenClient;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

public class PhotoGenService {

    private final PhotoGenClient client;
    private final int width;
    private final int height;
    private final int steps;
    private final double guidance;
    private final double loraScale;
    @Nullable
    private final Long baseSeed;
    private final PhotoGenClient.RealismParams realismParams;
    private final int imagesPerRequest;

    public PhotoGenService(PhotoGenClient client,
                           int width,
                           int height,
                           int steps,
                           double guidance,
                           double loraScale,
                           @Nullable Long baseSeed,
                           PhotoGenClient.RealismParams realismParams,
                           int imagesPerRequest) {
        this.client = client;
        this.width = width;
        this.height = height;
        this.steps = steps;
        this.guidance = guidance;
        this.loraScale = loraScale;
        this.baseSeed = baseSeed;
        this.realismParams = realismParams;
        this.imagesPerRequest = imagesPerRequest;
    }

    public Mono<byte[]> generateOne(String prompt, String loraPath, long seed) {
        Long effectiveSeed = baseSeed != null ? baseSeed + Math.abs(seed % 10_000) : seed;
        PhotoGenClient.GenerationParams params = new PhotoGenClient.GenerationParams(width, height, steps, guidance, loraScale,
                effectiveSeed);
        return client.generateWithLora(prompt, loraPath, params)
                .flatMap(url -> client.download(url)); // берём байты напрямую
    }

    public int imagesPerRequest() {
        return imagesPerRequest;
    }
}
