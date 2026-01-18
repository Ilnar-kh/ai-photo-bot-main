package com.aiphoto.bot.core.port.external;

import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.Map;

public interface TelegramClient {

    Mono<Void> sendMessage(Long chatId, String message, Map<String, Object> extra);

    default Mono<Void> sendMessage(Long chatId, String message) {
        return sendMessage(chatId, message, Map.of());
    }

    default Mono<Void> sendMessage(long chatId, String message, Map<String, Object> extra) {
        return sendMessage(Long.valueOf(chatId), message, extra);
    }

    default Mono<Void> sendMessage(long chatId, String message) {
        return sendMessage(Long.valueOf(chatId), message, Map.of());
    }

    Mono<Void> sendPhoto(Long chatId, byte[] jpegOrPng, String caption);

    Mono<Void> answerCallback(String callbackId);

    default Mono<Void> sendPhotoFromResources(Long chatId,
                                              String resourcePath,
                                              String caption,
                                              Map<String, Object> extra) {
        try (InputStream is = TelegramClient.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                return Mono.error(new IllegalStateException("Resource not found: " + resourcePath));
            }
            byte[] bytes = is.readAllBytes();
            return sendPhoto(chatId, bytes, caption, extra);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    Mono<Void> sendPhoto(Long chatId, byte[] jpegOrPng, String caption, Map<String, Object> extra);
}
