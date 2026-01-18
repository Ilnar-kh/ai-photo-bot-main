package com.aiphoto.bot.core.port.external;

import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

public interface PhotoGenClient {

    record GenerationParams(int width, int height, int steps, double guidance, double loraScale,
                            @Nullable Long seed) {
    }

    record RealismParams(int steps, double strength) {
    }

    Mono<String> generateWithLora(String prompt, String loraPath, GenerationParams params);

    Mono<byte[]> applyRealism(String imageUrl, RealismParams params);

    Mono<byte[]> download(String url);
}
