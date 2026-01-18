package com.aiphoto.bot.adapter.web.controller;

import com.aiphoto.bot.core.domain.Upload;
import com.aiphoto.bot.core.service.OrderService;
import com.aiphoto.bot.adapter.web.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/uploads")
public class UploadController {
    private final StorageService storage;
    private final OrderService orders;

    public UploadController(StorageService storage, OrderService orders) {
        this.storage = storage;
        this.orders = orders;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Upload> upload(@PathVariable UUID orderId,
                                         @RequestParam("file") MultipartFile file) throws Exception {
        String objectKey = orderId + "/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
        storage.put(objectKey, file.getContentType(), file.getInputStream(), file.getSize());
        Upload saved = orders.addUpload(orderId, objectKey, file.getContentType());

        orders.tryQueueAfterUpload(orderId);

        return ResponseEntity.created(URI.create("/api/orders/" + orderId + "/uploads/" + saved.id()))
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<?> list(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orders.listUploads(orderId));
    }

    // --- ОБРАБОТКА ОШИБОК ---

    // 404 — заказ не найден
    @ExceptionHandler(OrderService.NotFoundException.class)
    public ResponseEntity<?> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(404).body(
                Map.of(
                        "error", "Not Found",
                        "message", ex.getMessage()
                )
        );
    }

    // 403 — заказ не оплачен
    @ExceptionHandler(OrderService.ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(RuntimeException ex) {
        return ResponseEntity.status(403).body(
                Map.of(
                        "error", "Forbidden",
                        "message", ex.getMessage()
                )
        );
    }
}