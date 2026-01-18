package com.aiphoto.bot.adapter.telegram;

import com.aiphoto.bot.core.port.external.TelegramClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramClientImpl implements TelegramClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramClientImpl.class);

    private final WebClient webClient;
    private final String token;
    private final String apiBase;
    private final ObjectMapper objectMapper;

    public TelegramClientImpl(
            WebClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${telegram.bot.api-url:https://api.telegram.org}") String apiUrl,
            @Value("${telegram.bot.token}") String token
    ) {
        this.objectMapper = objectMapper;
        this.webClient = builder.baseUrl(apiUrl).build();
        this.token = token;
        this.apiBase = apiUrl;
    }

    // =====================================================================
    // Base JSON methods
    // =====================================================================

    @Override
    public Mono<Void> sendMessage(Long chatId, String text, Map<String, Object> extra) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        payload.put("parse_mode", "Markdown");
        if (extra != null && !extra.isEmpty()) {
            payload.putAll(extra);
        }
        return executeJson("sendMessage", payload);
    }

    public Mono<Void> sendMessage(Long chatId, String text) {
        return sendMessage(chatId, text, Map.of());
    }

    @Override
    public Mono<Void> answerCallback(String callbackId) {
        Map<String, Object> payload = Map.of("callback_query_id", callbackId);
        return executeJson("answerCallbackQuery", payload);
    }

    public Mono<Void> sendInvoice(Long chatId,
                                  String title,
                                  String description,
                                  String payload,
                                  String providerToken,
                                  String currency,
                                  java.util.List<Map<String, Object>> prices) {

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("title", title);
        body.put("description", description);
        body.put("payload", payload);
        body.put("provider_token", providerToken);
        body.put("currency", currency);
        body.put("prices", prices);

        return executeJson("sendInvoice", body);
    }

    public Mono<Void> answerPreCheckoutQuery(String id, boolean ok, String errorMessage) {
        Map<String, Object> body = new HashMap<>();
        body.put("pre_checkout_query_id", id);
        body.put("ok", ok);
        if (!ok && errorMessage != null) {
            body.put("error_message", errorMessage);
        }
        return executeJson("answerPreCheckoutQuery", body);
    }

    private Mono<Void> executeJson(String method, Map<String, Object> payload) {
        return webClient.post()
                .uri("/bot" + token + "/" + method)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(body -> log.info("Telegram {} request={}, response={}", method, payload, body))
                .doOnError(e -> log.error("Failed to call Telegram method {}", method, e))
                .then();
    }

    // =====================================================================
    // Photos (multipart)
    // =====================================================================

    @Override
    public Mono<Void> sendPhoto(Long chatId, byte[] jpegOrPng, String caption) {
        return sendPhoto(chatId, jpegOrPng, caption, Map.of());
    }

    @Override
    public Mono<Void> sendPhoto(Long chatId, byte[] jpegOrPng, String caption, Map<String, Object> extra) {
        MultipartBodyBuilder mb = new MultipartBodyBuilder();

        mb.part("chat_id", String.valueOf(chatId));

        mb.part("photo", new ByteArrayResource(jpegOrPng) {
                    @Override
                    public String getFilename() {
                        return "photo.jpg";
                    }
                })
                .filename("photo.jpg")
                .contentType(MediaType.IMAGE_JPEG);

        if (caption != null && !caption.isBlank()) {
            mb.part("caption", caption);
            mb.part("parse_mode", "Markdown");
        }

        if (extra != null) {
            Object rm = extra.get("reply_markup");
            if (rm != null) {
                try {
                    String rmJson = objectMapper.writeValueAsString(rm);
                    // ВАЖНО: как JSON, иначе Telegram часто “проглатывает”
                    mb.part("reply_markup", rmJson).contentType(MediaType.APPLICATION_JSON);

                    log.info("SENDPHOTO reply_markup JSON: {}", rmJson);
                } catch (Exception e) {
                    return Mono.error(e);
                }
            }
        }

        return webClient.post()
                .uri("/bot" + token + "/sendPhoto")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(mb.build()))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(resp -> log.info("Telegram sendPhoto resp={}", resp))
                .then();
    }

    // =====================================================================
    // Files
    // =====================================================================

    public Mono<byte[]> downloadFile(String fileId) {
        return getFilePath(fileId)
                .flatMap(path -> webClient.get()
                        .uri("/file/bot" + token + "/" + path)
                        .retrieve()
                        .bodyToMono(byte[].class));
    }

    private Mono<String> getFilePath(String fileId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bot" + token + "/getFile")
                        .queryParam("file_id", fileId)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> {
                    Object result = resp.get("result");
                    if (!(result instanceof Map<?, ?> r)) {
                        throw new IllegalStateException("No result in getFile response");
                    }
                    Object filePath = r.get("file_path");
                    if (filePath == null) {
                        throw new IllegalStateException("No file_path in getFile response");
                    }
                    return filePath.toString();
                });
    }

    public Mono<String> getFileUrl(String fileId) {
        return getFilePath(fileId)
                .map(path -> apiBase + "/file/bot" + token + "/" + path);
    }
}