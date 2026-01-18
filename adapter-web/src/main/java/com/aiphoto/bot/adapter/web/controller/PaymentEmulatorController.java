package com.aiphoto.bot.adapter.web.controller;

import com.aiphoto.bot.core.domain.Order;
import com.aiphoto.bot.core.port.external.TelegramClient;
import com.aiphoto.bot.core.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pay")
public class PaymentEmulatorController {

    private static final Logger log = LoggerFactory.getLogger(PaymentEmulatorController.class);

    private final OrderService orders;
    private final TelegramClient telegramClient;

    public PaymentEmulatorController(OrderService orders, TelegramClient telegramClient) {
        this.orders = orders;
        this.telegramClient = telegramClient;
    }

    @GetMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@RequestParam UUID orderId) {
        Order paid = orders.markPaid(orderId);
        long telegramId = orders.findTelegramIdByOrder(orderId);
        telegramClient.sendMessage(telegramId,
                        "Оплата прошла ✅\n" +
                                "Загрузите 10–30 фото для обучения модели:\n" +
                                "POST /api/orders/" + orderId + "/uploads (multipart: file)")
                .doOnError(e -> log.warn("Failed to send payment confirmation for order {}", orderId, e))
                .subscribe();
        return ResponseEntity.ok(Map.of(
                "status", "paid",
                "orderId", paid.id()
        ));
    }

    @ExceptionHandler(OrderService.NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(404).body(Map.of(
                "error", "Not Found",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler({OrderService.ForbiddenException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleIllegalState(RuntimeException ex) {
        return ResponseEntity.status(400).body(Map.of(
                "error", "Bad Request",
                "message", ex.getMessage()
        ));
    }
}
