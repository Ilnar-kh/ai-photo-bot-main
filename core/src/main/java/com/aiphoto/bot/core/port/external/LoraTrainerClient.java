package com.aiphoto.bot.core.port.external;

import reactor.core.publisher.Mono;

import java.util.List;

public interface LoraTrainerClient {

    record TrainingRequest(String imagesDataUrl, String triggerPhrase, int steps) {
    }

    record TrainingResult(String loraPath, String configUrl) {
    }

    Mono<TrainingResult> train(TrainingRequest request);
}
