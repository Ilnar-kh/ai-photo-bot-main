package com.aiphoto.bot.adapter.web.controller;

import com.aiphoto.bot.core.domain.Order;
import com.aiphoto.bot.core.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService s) {
        this.orderService = s;
    }

    public record CreateOrderRequest(long telegramId) {
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody CreateOrderRequest req) {
        Order order = orderService.createOrder(req.telegramId());
        return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(order);
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Order> pay(@PathVariable UUID orderId) {
        Order updated = orderService.markPaid(orderId);
        return ResponseEntity.ok(updated);
    }

    // ——— новые минимальные эндпоинты
    @PostMapping("/{orderId}/process")
    public ResponseEntity<Order> process(@PathVariable UUID orderId) {
        Order updated = orderService.markProcessing(orderId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{orderId}/done")
    public ResponseEntity<Order> done(@PathVariable UUID orderId) {
        Order updated = orderService.markDone(orderId);
        return ResponseEntity.ok(updated);
    }

    // ——— обработка ошибок
    @ExceptionHandler(OrderService.NotFoundException.class)
    public ResponseEntity<?> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(404).body(Map.of(
                "error", "Not Found",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(OrderService.ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(RuntimeException ex) {
        return ResponseEntity.status(403).body(Map.of(
                "error", "Forbidden",
                "message", ex.getMessage()
        ));
    }
}