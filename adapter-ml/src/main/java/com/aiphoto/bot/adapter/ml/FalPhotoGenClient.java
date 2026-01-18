package com.aiphoto.bot.adapter.ml;

import com.aiphoto.bot.adapter.ml.config.FalClientProperties;
import com.aiphoto.bot.core.port.external.PhotoGenClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FalPhotoGenClient implements PhotoGenClient {

    private static final Logger log = LoggerFactory.getLogger(FalPhotoGenClient.class);

    private final WebClient falWebClient;
    private final WebClient downloadWebClient;
    private final FalClientProperties properties;

    public FalPhotoGenClient(WebClient falWebClient,
                             WebClient downloadWebClient,
                             FalClientProperties properties) {
        this.falWebClient = falWebClient;
        this.downloadWebClient = downloadWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<String> generateWithLora(String prompt, String loraPath, GenerationParams params) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("prompt", prompt);
        body.put("width", params.width());
        body.put("height", params.height());
        body.put("guidance_scale", params.guidance());
        body.put("num_inference_steps", params.steps());
        if (params.seed() != null) {
            body.put("seed", params.seed());
        }
        body.put("loras", List.of(Map.of(
                "path",  loraPath,
                "scale", params.loraScale()
        )));

        return falWebClient.post()
                .uri("/" + properties.getGeneration().getEndpoint()) // должно быть fal-ai/flux-krea-lora
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(properties.getTimeoutSec()))
                .map(this::extractImageUrl)
                .flatMap(url -> url.map(Mono::just)
                        .orElseGet(() -> Mono.error(new IllegalStateException("Fal generation did not return image url"))));
    }

    @Override
    public Mono<byte[]> applyRealism(String imageUrl, RealismParams params) {
        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("image_url", imageUrl);
        input.put("num_inference_steps", params.steps());   // было "steps"
        input.put("lora_scale", params.strength());         // было "strength" -> мапим на lora_scale
        input.put("guidance_scale", 3.5);                   // опционально, можно вынести в конфиг
        payload.put("input", input);

        return falWebClient.post()
                .uri("/" + properties.getRealism().getEndpoint()) // убедись, что тут "fal-ai/image-editing/realism"
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                // дальше как у тебя: timeout + extractBase64Image()/extractImageUrl(...)
                .timeout(Duration.ofSeconds(properties.getTimeoutSec()))
                .flatMap(node -> {
                    Optional<byte[]> base64 = extractBase64Image(node);
                    if (base64.isPresent()) {
                        return Mono.just(base64.get());
                    }
                    return extractImageUrl(node)
                            .map(this::download)
                            .orElseGet(() -> Mono.error(new IllegalStateException("Fal realism did not return image")));
                });
    }

    private Optional<String> extractImageUrl(JsonNode node) {
        if (node == null) {
            return Optional.empty();
        }

        // 1) Твой случай: images в корне ответа
        JsonNode rootImages = node.path("images");
        if (rootImages.isArray()) {
            for (JsonNode image : rootImages) {
                String url = image.path("url").asText(null);
                if (url != null && !url.isBlank()) {
                    return Optional.of(url);
                }
            }
        }

        // 2) Запасной путь: response / output (на случай других моделей)
        JsonNode container = node.path("response");
        if (container.isMissingNode()) {
            container = node.path("output");
        }
        if (!container.isMissingNode()) {
            JsonNode images = container.path("images");
            if (images.isArray()) {
                for (JsonNode image : images) {
                    String url = image.path("url").asText(null);
                    if (url != null && !url.isBlank()) {
                        return Optional.of(url);
                    }
                }
            }

            String direct = container.path("image_url").asText(null);
            if (direct != null && !direct.isBlank()) {
                return Optional.of(direct);
            }
        }

        // 3) Старый фоллбек: root.image = "https://..."
        String imageField = node.path("image").asText(null);
        if (imageField != null && !imageField.isBlank()) {
            return Optional.of(imageField);
        }

        return Optional.empty();
    }

    private Optional<byte[]> extractBase64Image(JsonNode node) {
        if (node == null) {
            return Optional.empty();
        }
        JsonNode response = node.path("response");
        if (response.isMissingNode()) {
            response = node.path("output");
        }
        if (!response.isMissingNode()) {
            String base64 = response.path("image_base64").asText(null);
            if (base64 != null && !base64.isBlank()) {
                return Optional.of(decode(base64));
            }
            JsonNode images = response.path("images");
            if (images.isArray()) {
                for (JsonNode image : images) {
                    String base64Image = image.path("content").asText(null);
                    if (base64Image != null && !base64Image.isBlank()) {
                        return Optional.of(decode(base64Image));
                    }
                }
            }
        }
        String rootBase64 = node.path("image_base64").asText(null);
        if (rootBase64 != null && !rootBase64.isBlank()) {
            return Optional.of(decode(rootBase64));
        }
        return Optional.empty();
    }

    private byte[] decode(String base64) {
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to decode base64 image", ex);
            throw ex;
        }
    }

    public Mono<byte[]> download(String url) {
        return downloadWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(properties.getTimeoutSec()));
    }
}
