package com.aiphoto.bot.adapter.telegram.scheduler;

import com.aiphoto.bot.adapter.persistence.repository.jpa.OrderJpaRepository;
import com.aiphoto.bot.adapter.telegram.TelegramClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class OfferFollowupScheduler {

    private static final Logger log = LoggerFactory.getLogger(OfferFollowupScheduler.class);

    private static final int PRICE_30MIN_RUB = 499;

    private final OrderJpaRepository orderJpaRepository;
    private final TelegramClientImpl telegramClient;

    public OfferFollowupScheduler(OrderJpaRepository orderJpaRepository,
                                  TelegramClientImpl telegramClient) {
        this.orderJpaRepository = orderJpaRepository;
        this.telegramClient = telegramClient;
    }

    private static final String FOLLOWUP_30MIN_TEXT = """
            По себе знаем, что не всегда просто
            решиться на покупку.
            Поэтому предлагаем пакет за *%d₽* в
            месяц:

            ✅ Модель (цифровой двойник,
            созданный на основе ваших снимков для
            генерации новых фото с вами)
            ✅ 18 фото в готовых стилях или по
            вашему описанию

            После этого сможете решить, хотите ли
            докупить ещё фото 👍
            """;

    private static final String FOLLOWUP_24H_QUESTION = """
            🤔 *Что вас остановило от покупки?*

            Мы хотим сделать наш сервис
            максимально удобным и полезным.
            Пожалуйста, выберите причину ниже —
            возможно, у нас есть специальное
            предложение для вас 😉

            👇 Нажмите на одну из кнопок ниже:
            """;

    @Transactional
    @Scheduled(fixedDelay = 60_000)
    public void tick() {
        Instant now = Instant.now();
        sendAfter30Min(now);
        sendAfter24HQuestionOnly(now);
    }

    /**
     * 30 минут считаем от offerShownAt:
     * offerShownAt <= now-30m, purchasedAt is null, followup30SentAt is null
     */
    private void sendAfter30Min(Instant now) {
        Instant deadline = now.minus(Duration.ofMinutes(30));

        List<OrderJpaRepository.FollowupRow> rows = orderJpaRepository.findForFollowup30Rows(deadline);
        if (!rows.isEmpty()) {
            log.info("Followup30 candidates={}", rows.size());
        }

        for (var r : rows) {
            UUID orderId = r.getOrderId();
            Long telegramId = r.getTelegramId(); // в личке это chatId

            int updated = orderJpaRepository.setFollowup30SentAtIfNull(orderId, now, now);
            if (updated != 1) continue;

            String text = FOLLOWUP_30MIN_TEXT.formatted(PRICE_30MIN_RUB);

            Map<String, Object> kb = inlineKeyboard(List.of(List.of(
                    button("1 модель и 18 фото | " + PRICE_30MIN_RUB + "₽", "BUY_FOLLOWUP_30|" + orderId)
            )));

            telegramClient.sendMessage(telegramId, text, kb)
                    .doOnSuccess(v -> log.info("Sent followup30 telegramId={} orderId={}", telegramId, orderId))
                    .onErrorResume(e -> {
                        log.warn("Failed followup30 telegramId={} orderId={}", telegramId, orderId, e);
                        return Mono.empty();
                    })
                    .subscribe();
        }
    }

    /**
     * 24 часа — отправляем ТОЛЬКО вопрос с причинами.
     * Ответы по причинам (PRICE_HIGH/QUALITY/PRIVACY/...) должны уходить из controller по callback FOLLOWUP_REASON|...
     */
    private void sendAfter24HQuestionOnly(Instant now) {
        Instant deadline = now.minus(Duration.ofHours(24));

        List<OrderJpaRepository.FollowupRow> rows = orderJpaRepository.findForFollowup24Rows(deadline);
        if (!rows.isEmpty()) {
            log.info("Followup24 candidates={}", rows.size());
        }

        for (var r : rows) {
            UUID orderId = r.getOrderId();
            Long telegramId = r.getTelegramId();

            int updated = orderJpaRepository.setFollowup24SentAtIfNull(orderId, now, now);
            if (updated != 1) continue;

            Map<String, Object> reasonsKb = inlineKeyboard(List.of(
                    List.of(button("💸 Цена слишком высокая", "FOLLOWUP_REASON|PRICE_HIGH|" + orderId)),
                    List.of(button("📸 Сомневаюсь в качестве", "FOLLOWUP_REASON|QUALITY|" + orderId)),
                    List.of(button("🔒 Опасаюсь за конфиденциальность", "FOLLOWUP_REASON|PRIVACY|" + orderId)),
                    List.of(button("🧑‍💻 Не вижу смысла", "FOLLOWUP_REASON|NO_NEED|" + orderId)),
                    List.of(button("🤔 Другое", "FOLLOWUP_REASON|OTHER|" + orderId))
            ));

            telegramClient.sendMessage(telegramId, FOLLOWUP_24H_QUESTION, reasonsKb)
                    .doOnSuccess(v -> log.info("Sent followup24 question telegramId={} orderId={}", telegramId, orderId))
                    .onErrorResume(e -> {
                        log.warn("Failed followup24 question telegramId={} orderId={}", telegramId, orderId, e);
                        return Mono.empty();
                    })
                    .subscribe();
        }
    }

    private Map<String, Object> inlineKeyboard(List<List<Map<String, Object>>> rows) {
        return Map.of("reply_markup", Map.of("inline_keyboard", rows));
    }

    private Map<String, Object> button(String text, String callbackData) {
        Map<String, Object> btn = new LinkedHashMap<>();
        btn.put("text", text);
        btn.put("callback_data", callbackData);
        return btn;
    }
}