package com.aiphoto.bot.adapter.ml;

import com.aiphoto.bot.adapter.ml.config.FalClientProperties;
import com.aiphoto.bot.core.port.external.LoraTrainerClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class FalLoraTrainerClient implements LoraTrainerClient {

    private static final Logger log = LoggerFactory.getLogger(FalLoraTrainerClient.class);
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(10);

    private final WebClient falWebClient;
    private final FalClientProperties properties;

    public FalLoraTrainerClient(WebClient falWebClient, FalClientProperties properties) {
        this.falWebClient = falWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<TrainingResult> train(TrainingRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("images_data_url", request.imagesDataUrl());
        body.put("trigger_phrase",  request.triggerPhrase());
        body.put("steps",           request.steps());

        log.info("FAL training payload: {}", body);

        return falWebClient.post()
                .uri("/" + properties.getTraining().getEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(node ->
                        log.info("FAL training response: {}", node)
                )
                .map(node -> {
                    JsonNode diff = node.path("diffusers_lora_file");
                    JsonNode cfg  = node.path("config_file");

                    String loraUrl   = diff.path("url").asText("");
                    String configUrl = cfg.path("url").asText("");

                    if (loraUrl.isBlank()) {
                        throw new IllegalStateException(
                                "No diffusers_lora_file.url in FAL response"
                        );
                    }

                    return new TrainingResult(loraUrl, configUrl);
                });
    }
}

