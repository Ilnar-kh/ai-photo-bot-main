package com.aiphoto.bot.adapter.web.controller;

import com.aiphoto.bot.core.port.external.TelegramClient;
import com.aiphoto.bot.core.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/train")
public class TrainingEmulatorController {

    private static final Logger log = LoggerFactory.getLogger(TrainingEmulatorController.class);

    private final OrderService orders;
    private final TelegramClient telegramClient;

    public TrainingEmulatorController(OrderService orders, TelegramClient telegramClient) {
        this.orders = orders;
        this.telegramClient = telegramClient;
    }

    @PostMapping("/finish")
    public ResponseEntity<Void> finishTraining(@RequestParam UUID orderId) {
        long chatId = orders.findTelegramIdByOrder(orderId);
        Map<String, Object> keyboard = Map.of(
                "reply_markup", Map.of(
                        "inline_keyboard", styleButtons(orderId)
                )
        );
        telegramClient.sendMessage(chatId,
                        "Модель готова! Выберите стиль фотосессии:",
                        keyboard)
                .doOnError(e -> log.warn("Failed to send style menu for order {}", orderId, e))
                .subscribe();
        return ResponseEntity.ok().build();
    }

    private List<List<Map<String, Object>>> styleButtons(UUID orderId) {
        List<List<Map<String, Object>>> rows = new ArrayList<>();
        String[][] styles = new String[][]{
                {"🎬 Cinematic", "cinematic"},
                {"💡 Studio", "studio"},
                {"🌆 Neon", "neon"},
                {"🌿 Forest", "forest"},
                {"🏙️ Urban", "urban"},
                {"🌌 Galaxy", "galaxy"},
                {"🌊 Ocean", "ocean"},
                {"🔥 Cyberpunk", "cyberpunk"},
                {"🌸 Sakura", "sakura"},
                {"🧊 Nordic", "nordic"}
        };
        List<Map<String, Object>> currentRow = new ArrayList<>();
        for (String[] style : styles) {
            currentRow.add(Map.of(
                    "text", style[0],
                    "callback_data", "STYLE|" + orderId + "|" + style[1]
            ));
            if (currentRow.size() == 2) {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }
        return rows;
    }
}