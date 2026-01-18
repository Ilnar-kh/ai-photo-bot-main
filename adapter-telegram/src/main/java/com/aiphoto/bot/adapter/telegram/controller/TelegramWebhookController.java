package com.aiphoto.bot.adapter.telegram.controller;

import com.aiphoto.bot.adapter.telegram.Keyboard;
import com.aiphoto.bot.adapter.telegram.TelegramClientImpl;
import com.aiphoto.bot.core.domain.Gender;
import com.aiphoto.bot.core.domain.LoraStatus;
import com.aiphoto.bot.core.domain.Order;
import com.aiphoto.bot.core.domain.Upload;
import com.aiphoto.bot.core.port.persistence.OrderRepository;
import com.aiphoto.bot.core.service.LoraTrainerService;
import com.aiphoto.bot.core.service.OrderService;
import com.aiphoto.bot.core.service.PhotoGenService;
import com.aiphoto.bot.core.service.Styles;
import com.aiphoto.bot.core.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/webhook/telegram")
public class TelegramWebhookController {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookController.class);
    private static final String NEXT_BUTTON = "Далее ▶️";

    // === НОВОЕ: лимит ИИ-фото на заказ ===
    private static final int MAX_GENERATED_PHOTOS = 60;

    // === НОВОЕ: цены в рублях ===
    private static final int PRICE_MAIN_RUB = 899;    // первая покупка
    private static final int PRICE_RETRAIN_RUB = 399; // переобучение
    private static final int PRICE_EXTRA_RUB = 299;   // доп. пакет

    // =====================================================================
    // НОВОЕ: добивки (30 минут / 24 часа)
    // =====================================================================
    private static final int PRICE_FOLLOWUP_30_RUB = 499;
    private static final int PRICE_FOLLOWUP_24_RUB = 480;

    // payloads (для Telegram Payments)
    private static final String PAYLOAD_FOLLOWUP_30 = "FOLLOWUP30|";
    private static final String PAYLOAD_FOLLOWUP_24 = "FOLLOWUP24|";

    private static final String FOLLOWUP_24H_ANSWER_PRICE = """
            💸 Думаете, что это дорого? Давайте
            объясним, за что вы платите.
            
            Для создания ваших фото мы используем
            современные нейросети и видеокарту
            уровня *H100* — эти мощности работают
            *для вас*, чтобы создать реалистичные и
            детализированные образы.
            
            Мы понимаем, что цена имеет значение,
            поэтому сейчас у вас есть возможность
            воспользоваться специальным
            предложением:
            
            🎉 *1 модель + 18 фото* — всего *%d₽*.
            
            Нажмите кнопку ниже, если хотите забрать по сниженной цене 👇
            """;

    private static final String FOLLOWUP_24H_ANSWER_QUALITY = """
            📸 Сомневаетесь в качестве — это нормально.
            
            Результат зависит от качества и разнообразия ваших фото.
            Если будет “не похоже” — поддержка поможет и можно переобучить модель.
            
            Хотите попробовать безопасно:
            🎉 *1 модель + 18 фото* — за *%d₽* 👇
            """;

    private static final String FOLLOWUP_24H_ANSWER_PRIVACY = """
            🔒 Про конфиденциальность — справедливый вопрос.
            
            Ваши фото используются только для создания вашей персональной модели.
            Мы не публикуем их и не делаем доступными другим пользователям.
            
            Если хотите попробовать с минимальным риском:
            🎉 *1 модель + 18 фото* — за *%d₽* 👇
            """;

    private static final String FOLLOWUP_24H_ANSWER_NO_NEED = """
            🧑‍💻 Понял — может казаться, что “не нужно”.
            
            Обычно берут ради:
            • новых аватарок и контента
            • фото в разных стилях без студии
            • экономии времени/денег на съемках
            
            Можете протестировать лёгким пакетом:
            🎉 *1 модель + 18 фото* — за *%d₽* 👇
            """;

    private static final String FOLLOWUP_24H_ANSWER_OTHER = """
            🤔 Окей! Напишите одним сообщением, что именно смутило — я передам это команде.
            
            А если хотите попробовать с минимальным входом:
            🎉 *1 модель + 18 фото* — за *%d₽* 👇
            """;

    // Ждём текст промпта от конкретного пользователя
    private final Map<Long, UUID> awaitingPrompt = new java.util.concurrent.ConcurrentHashMap<>();

    private final OrderService orders;
    private final UploadService uploadService;
    private final TelegramClientImpl telegramClient;
    private final PhotoGenService photoGenService;
    private final LoraTrainerService loraTrainerService;
    private final OrderRepository orderRepository;
    private final int minPhotos;
    private final int maxPhotos;
    private final int trainingEtaMinutes;

    // === НОВОЕ: токен провайдера платежей (YooKassa / YooMoney для Telegram Payments) ===
    @Value("${telegram.payments.provider-token}")
    private String providerToken;

    String EXAMPLE_1_CAPTION = """
            *Полина, 22 года*  
            Небольшой блог, любит фотосессии и эстетику.
            
            Полине хотелось обновить фото для соцсетей,  
            но студийные съёмки — дорого и долго.  
            Хотелось больше естественных, стильных образов  
            и оригинальных фотографий в разных локациях 🌿
            
            После генерации была приятно удивлена  
            реалистичностью и деталями фотосессий.
            
            Полина обновила ленту, получила много лайков  
            и теперь регулярно создаёт контент в нашем боте 💫
            """;

    String EXAMPLE_2_CAPTION = """
            *Мария, 33 года*  
            Есть семья и дети.
            
            Свободного времени почти нет — работа и семья  
            занимают большую часть дня.  
            Марии хотелось снова увидеть себя красивой,  
            в новых стилях и необычных образах ✨
            
            Сгенерировала несколько фотосессий,  
            получила десятки студийных образов  
            и осталась очень довольна результатом.
            
            Позже даже решилась сменить образ —  
            получила море комплиментов 💖
            """;

    public TelegramWebhookController(OrderService orders,
                                     UploadService uploadService,
                                     TelegramClientImpl telegramClient,
                                     PhotoGenService photoGenService,
                                     LoraTrainerService loraTrainerService,
                                     OrderRepository orderRepository,
                                     @Value("${fal.business.min-photos:10}") int minPhotos,
                                     @Value("${fal.business.max-photos:30}") int maxPhotos,
                                     @Value("${fal.business.training-eta-min:10}") int trainingEtaMinutes) {
        this.orders = orders;
        this.uploadService = uploadService;
        this.telegramClient = telegramClient;
        this.photoGenService = photoGenService;
        this.loraTrainerService = loraTrainerService;
        this.orderRepository = orderRepository;
        this.minPhotos = minPhotos;
        this.maxPhotos = maxPhotos;
        this.trainingEtaMinutes = trainingEtaMinutes;
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<Void> handleUpdate(@RequestBody Map<String, Object> update) {
        log.info("Received telegram update: {}", update);
        if (update == null) return ResponseEntity.ok().build();

        Optional.ofNullable(update.get("message"))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .ifPresent(this::handleMessage);

        Optional.ofNullable(update.get("callback_query"))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .ifPresent(this::handleCallback);

        Optional.ofNullable(update.get("pre_checkout_query"))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .ifPresent(this::handlePreCheckoutQuery);

        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private void handleMessage(Map<String, Object> message) {
        Long chatId = extractChatId(message);
        Long telegramId = extractTelegramId(message);

        @SuppressWarnings("unchecked")
        Map<String, Object> cb = (Map<String, Object>) message.get("callback_query");
        if (cb != null) {
            handleCallback(cb);
            return;
        }

        if (chatId == null || telegramId == null) return;

        Map<String, Object> successPayment = Optional.ofNullable(message.get("successful_payment"))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .orElse(null);
        if (successPayment != null) {
            handleSuccessfulPayment(chatId, telegramId, successPayment);
            return;
        }

        String text = Optional.ofNullable(message.get("text"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(String::trim)
                .orElse(null);

        if ("/start".equalsIgnoreCase(text)) {
            awaitingPrompt.remove(telegramId);
        }

        UUID awaitingOrder = awaitingPrompt.get(telegramId);
        if (awaitingOrder != null
                && text != null
                && !text.isBlank()
                && !text.startsWith("/")) {
            String clean = text.strip();
            if (clean.length() > 800) clean = clean.substring(0, 800);

            if (isPhotoLimitReached(awaitingOrder)) {
                sendBuyMorePhotosMessage(chatId, awaitingOrder);
                awaitingPrompt.remove(telegramId);
                return;
            }

            generateAndSendImages(awaitingOrder, chatId, clean, "📝 Промпт");
            awaitingPrompt.remove(telegramId);
            return;
        }

        if (handleMediaUploads(message, chatId, telegramId)) {
            return;
        }

        if (text == null) return;

        if ("/start".equalsIgnoreCase(text)) {
            orders.findLatestReadyOrderForTelegramUser(telegramId).ifPresentOrElse(readyOrder -> {
                sendReadyUI(chatId, readyOrder.id());
            }, () -> {
                orders.createOrder(telegramId);
                telegramClient.sendPhotoFromResources(
                                chatId,
                                "bot/offer.jpg",
                                """
                                        Привет! Я — твоя AI-фотостудия 📸
                                        Создам реалистичную фотосессию в любом образе. Жми *Далее*, расскажу как всё устроено.
                                        """,
                                Keyboard.inline(new Keyboard.InlineBtn(NEXT_BUTTON, "NEXT"))
                        )
                        .subscribe();
            });
            return;
        }

        if (NEXT_BUTTON.equals(text)) {
            sendExamplesThenOffer(chatId, telegramId);
            return;
        }

        handlePromptMessage(chatId, telegramId, text);
    }

    private void sendExamplesThenOffer(Long chatId, Long telegramId) {
        telegramClient.sendPhotoFromResources(
                        chatId,
                        "bot/examples/example1.jpg",
                        EXAMPLE_1_CAPTION,
                        Map.of()
                )
                .then(
                        telegramClient.sendPhotoFromResources(
                                chatId,
                                "bot/examples/example2.jpg",
                                EXAMPLE_2_CAPTION,
                                Map.of()
                        )
                )
                .then(
                        sendOfferMono(chatId, telegramId)   // 👈 ВАЖНО
                )
                .doOnError(e -> log.warn("Failed to send examples flow", e))
                .subscribe();
    }

    @SuppressWarnings("unchecked")
    private Long extractTelegramIdFromCallback(Map<String, Object> callback) {
        Object fromObj = callback.get("from");
        if (!(fromObj instanceof Map<?, ?> from)) return null;

        Object idObj = from.get("id");
        if (idObj instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void handleCallback(Map<String, Object> callback) {
        String callbackId = Optional.ofNullable(callback.get("id"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElse(null);

        String data = Optional.ofNullable(callback.get("data"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElse(null);

        Map<String, Object> message = Optional.ofNullable(callback.get("message"))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .orElse(Map.of());

        Long chatId = extractChatId(message);
        Long telegramId = extractTelegramIdFromCallback(callback);

        if (callbackId != null) {
            telegramClient.answerCallback(callbackId)
                    .doOnError(e -> log.warn("Failed to answer callback {}", callbackId, e))
                    .subscribe();
        }

        if (data == null || chatId == null || telegramId == null) return;

        if ("NEXT".equals(data)) {
            sendExamplesThenOffer(chatId, telegramId); // важно: именно chain
            return;
        }

        if (data.startsWith("BUY|")) {
            handleBuyCallback(chatId, data.substring("BUY|".length()));
            return;
        }

        if (data.startsWith("FAKEPAY|")) {
            handleFakePayCallback(chatId, data.substring("FAKEPAY|".length()));
            return;
        }

        if (data.startsWith("GENDER|")) {
            handleGenderCallback(chatId, data.substring("GENDER|".length()));
            return;
        }

        if (data.startsWith("TRAIN|")) {
            handleTrainCallback(chatId, data.substring("TRAIN|".length()));
            return;
        }

        if (data.startsWith("RETRAIN_PREPAY|")) {
            UUID readyOrderId = parseOrderId(data.substring("RETRAIN_PREPAY|".length()));
            if (readyOrderId == null) {
                telegramClient.sendMessage(chatId, "Заказ не найден. Попробуйте позже.").subscribe();
                return;
            }
            Order newOrder = orders.createOrder(telegramId);

            telegramClient.sendMessage(
                            chatId,
                            """
                                    🔁 Переобучение модели
                                    
                                    Это улучшит схожесть и стабильность.
                                    Стоимость: *399 ₽* · Время: ~10–15 минут.
                                    
                                    Сейчас откроется форма оплаты:
                                    """)
                    .subscribe();

            telegramClient.sendInvoice(
                            chatId,
                            "Переобучение модели",
                            "Улучшение качества и стабильности персональной модели",
                            "RETRAIN|" + newOrder.id(),
                            providerToken,
                            "RUB",
                            List.of(priceItem("Переобучение модели", PRICE_RETRAIN_RUB))
                    )
                    .doOnError(e -> log.warn("Failed to send RETRAIN invoice for order {}", newOrder.id(), e))
                    .subscribe();
            return;
        }

        if (data.startsWith("STYLE|")) {
            handleStyleCallback(chatId, data);
            return;
        }

        if (data.startsWith("PROMPT|")) {
            handlePromptModeCallback(chatId, telegramId, data.substring("PROMPT|".length()));
            return;
        }

        if (data.startsWith("RETRAINPAY|")) {
            handleRetrainPay(chatId, telegramId, data.substring("RETRAINPAY|".length()));
            return;
        }

        if (data.startsWith("BUY_EXTRA|")) {
            handleBuyExtraCallback(chatId, data.substring("BUY_EXTRA|".length()));
            return;
        }

        // =====================================================================
        // НОВОЕ: добивки (кнопки и причины)
        // =====================================================================

        if (data.startsWith("BUY_FOLLOWUP_30|")) {
            handleBuyFollowup30Callback(chatId, data.substring("BUY_FOLLOWUP_30|".length()));
            return;
        }

        if (data.startsWith("BUY_FOLLOWUP_24|")) {
            handleBuyFollowup24Callback(chatId, data.substring("BUY_FOLLOWUP_24|".length()));
            return;
        }

        if (data.startsWith("FOLLOWUP_REASON|")) {
            handleFollowupReason(chatId, data);
        }
    }

    private void handleRetrainPay(Long chatId, Long telegramId, String orderIdRaw) {
        UUID orderId = parseOrderId(orderIdRaw);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Платёж не удалось обработать. Попробуйте позже.").subscribe();
            return;
        }

        try {
            orders.markPaid(orderId);
        } catch (RuntimeException ex) {
            log.warn("Failed to mark retrain order {} as paid", orderId, ex);
            telegramClient.sendMessage(chatId, "Не удалось принять оплату. Попробуйте позже.").subscribe();
            return;
        }

        try {
            orders.purgeUserDataForRetrain(telegramId, orderId);
        } catch (RuntimeException ex) {
            log.warn("Failed to purge user data for retrain [telegramId={}, orderId={}]", telegramId, orderId, ex);
        }

        telegramClient.sendMessage(
                chatId,
                ("Оплата принята ✅\nШаг 2/3 — загрузите *%d–%d фото* для переобучения.\n" +
                        "\n" +
                        "\uD83D\uDCF8 Какие фото нужны: \n" +
                        "• 10–30 ваших фото хорошего качества.  \n" +
                        "• Снимки должны быть разными: портреты, по пояс и в полный рост.  \n" +
                        "• Лицо хорошо видно: анфас, 3/4, профиль.  \n" +
                        "• Разное освещение, ракурсы и одежда.  \n" +
                        "• Без фильтров, масок, очков и сильных теней.  \n" +
                        "• Только вы на фото, без других людей.\n" +
                        "\n" +
                        "Разнообразные фото дают самую точную и красивую модель ✨ ")
                        .formatted(minPhotos, maxPhotos)
        ).subscribe();
    }

    private void handlePromptModeCallback(Long chatId, Long telegramId, String orderIdRaw) {
        UUID orderId = parseOrderId(orderIdRaw);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Заказ не найден. Попробуйте позже.")
                    .subscribe();
            return;
        }

        Optional<LoraStatus> s = orders.findLoraStatus(orderId);
        if (s.isEmpty() || s.get() != LoraStatus.READY) {
            telegramClient.sendMessage(chatId, "Модель ещё не готова. Дождитесь завершения обучения.")
                    .subscribe();
            return;
        }

        if (isPhotoLimitReached(orderId)) {
            sendBuyMorePhotosMessage(chatId, orderId);
            return;
        }

        awaitingPrompt.put(telegramId, orderId);

        telegramClient.sendMessage(
                chatId,
                """
                        Напишите свой промпт на английском ✍️
                        
                        Если нужно вдохновение — посмотрите готовые варианты:
                        """,
                buildPromptHelpKeyboard()
        ).subscribe();
    }

    private Map<String, Object> buildPromptHelpKeyboard() {
        List<List<Map<String, Object>>> rows = List.of(
                List.of(urlButton("📚 Готовые промты", "https://t.me/ii_photolab"))
        );
        return inlineKeyboard(rows);
    }

    private Map<String, Object> urlButton(String text, String url) {
        Map<String, Object> btn = new LinkedHashMap<>();
        btn.put("text", text);
        btn.put("url", url);
        return btn;
    }

    private Mono<Void> sendOfferMono(Long chatId, Long telegramId) {
        Order order = orders.findLatestOrderForTelegramUser(telegramId)
                .orElseGet(() -> orders.createOrder(telegramId));

        String caption = """
                🔥 *Суперпредложение: скидка 50%!*  
                Всего *899₽* вместо *1800₽* — забирай сейчас ✅
                
                При покупке вы получаете:
                • *1 персональная модель* (цифровой двойник на основе ваших фото)
                • *60 фотографий* в любом образе
                • *7 готовых стилей* на выбор
                • Фото по вашему описанию (любой образ)
                • Готовые описания для получения лучших фотографий
                • Реалистичность уровня профи
                
                🎁 Оплатите в течение *30 минут* и получите *+10 бонусных генераций*!
                
                Продолжим? 👇
                """;

        Map<String, Object> extra =
                Keyboard.inline(new Keyboard.InlineBtn(
                        "Купить 1 модель · 60 фото · 899₽",
                        "BUY|" + order.id()
                ));

        return telegramClient.sendPhotoFromResources(chatId, "bot/offer-paid.jpg", caption, extra)
                .doOnSuccess(v -> orderRepository.markOfferShownIfNull(order.id(), Instant.now()))
                .doOnError(e -> log.warn("Failed to send offer for order {}", order.id(), e));
    }

    private void handlePromptMessage(Long chatId, Long telegramId, String prompt) {
        if (prompt == null || prompt.isBlank()) return;

        Optional<Order> maybeOrder = orders.findLatestOrderForTelegramUser(telegramId);
        if (maybeOrder.isEmpty()) {
            telegramClient.sendMessage(chatId, "Создай заказ командой */start* — и к делу!")
                    .doOnError(e -> log.warn("Failed to notify about missing order for prompt {}", telegramId, e))
                    .subscribe();
            return;
        }
        Order order = maybeOrder.get();
        Optional<LoraStatus> loraStatus = orders.findLoraStatus(order.id());
        if (loraStatus.isEmpty() || loraStatus.get() != LoraStatus.READY) {
            telegramClient.sendMessage(chatId, "Модель ещё обучается ⏳ Я напишу, когда всё будет готово.")
                    .doOnError(e -> log.warn("Failed to notify about pending training for order {}", order.id(), e))
                    .subscribe();
            return;
        }

        if (isPhotoLimitReached(order.id())) {
            sendBuyMorePhotosMessage(chatId, order.id());
            return;
        }

        generateAndSendImages(order.id(), chatId, prompt, "📝 Промпт");
    }

    @SuppressWarnings("unchecked")
    private Long extractChatId(Map<String, Object> message) {
        Object chatObj = message.get("chat");
        if (!(chatObj instanceof Map)) return null;
        Map<String, Object> chat = (Map<String, Object>) chatObj;

        Object idObj = chat.get("id");
        if (idObj instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Long extractTelegramId(Map<String, Object> message) {
        Object fromObj = message.get("from");
        if (!(fromObj instanceof Map)) return null;
        Map<String, Object> from = (Map<String, Object>) fromObj;

        Object idObj = from.get("id");
        if (idObj instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    private Map<String, Object> priceItem(String label, int priceRub) {
        return Map.of("label", label, "amount", priceRub * 100);
    }

    private void handleBuyCallback(Long chatId, String orderIdRaw) {
        UUID orderId = parseOrderId(orderIdRaw);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Не удалось определить заказ. Попробуйте ещё раз позже.").subscribe();
            return;
        }

        telegramClient.sendInvoice(
                        chatId,
                        "AI-модель",
                        "Персональная модель · 60 фото",
                        "MAIN|" + orderId,
                        providerToken,
                        "RUB",
                        List.of(priceItem("Покупка модели", PRICE_MAIN_RUB))
                )
                .doOnError(e -> log.warn("Failed to send MAIN invoice for order {}", orderId, e))
                .subscribe();
    }

    // =====================================================================
    // НОВОЕ: покупка добивок (invoice)
    // =====================================================================

    private void handleBuyFollowup30Callback(Long chatId, String orderIdRaw) {
        UUID orderId = parseOrderId(orderIdRaw);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Не удалось определить заказ. Попробуйте позже.").subscribe();
            return;
        }

        telegramClient.sendInvoice(
                        chatId,
                        "AI-модель (спец-пакет)",
                        "1 модель + 18 фото",
                        PAYLOAD_FOLLOWUP_30 + orderId,
                        providerToken,
                        "RUB",
                        List.of(priceItem("1 модель + 18 фото", PRICE_FOLLOWUP_30_RUB))
                )
                .doOnError(e -> log.warn("Failed to send FOLLOWUP30 invoice for order {}", orderId, e))
                .subscribe();
    }

    private void handleBuyFollowup24Callback(Long chatId, String orderIdRaw) {
        UUID orderId = parseOrderId(orderIdRaw);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Не удалось определить заказ. Попробуйте позже.").subscribe();
            return;
        }

        telegramClient.sendInvoice(
                        chatId,
                        "AI-модель (спец-пакет)",
                        "1 модель + 18 фото",
                        PAYLOAD_FOLLOWUP_24 + orderId,
                        providerToken,
                        "RUB",
                        List.of(priceItem("1 модель + 18 фото", PRICE_FOLLOWUP_24_RUB))
                )
                .doOnError(e -> log.warn("Failed to send FOLLOWUP24 invoice for order {}", orderId, e))
                .subscribe();
    }

    private void handleFakePayCallback(Long chatId, String orderIdRaw) {
        UUID orderId = parseOrderId(orderIdRaw);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Не удалось обработать оплату. Попробуйте позже.").subscribe();
            return;
        }

        try {
            orders.markPaid(orderId);
        } catch (RuntimeException ex) {
            log.warn("Failed to mark order {} as paid", orderId, ex);
            telegramClient.sendMessage(chatId, "Не удалось принять оплату. Попробуйте позже.").subscribe();
            return;
        }

        telegramClient.sendMessage(chatId,
                        "Оплата принята ✅ Шаг 2/3 — загрузите *%d–%d фото* для обучения."
                                .formatted(minPhotos, maxPhotos))
                .subscribe();
    }

    private void handleTrainCallback(Long chatId, String orderIdRaw) {
        UUID orderId = parseOrderId(orderIdRaw);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Заказ не найден. Попробуйте позже.").subscribe();
            return;
        }

        List<Upload> uploads;
        try {
            uploads = uploadService.listUploads(orderId);
        } catch (RuntimeException ex) {
            log.warn("Failed to load uploads for order {}", orderId, ex);
            telegramClient.sendMessage(chatId, "Не удалось загрузить фото заказа. Попробуйте позже.").subscribe();
            return;
        }

        int count = uploads.size();
        if (count < minPhotos) {
            telegramClient.sendMessage(chatId, "Нужно минимум %d фото для обучения (сейчас %d).".formatted(minPhotos, count)).subscribe();
            return;
        }
        if (count > maxPhotos) {
            telegramClient.sendMessage(chatId, "Максимум %d фото для обучения (сейчас %d).".formatted(maxPhotos, count)).subscribe();
            return;
        }

        Optional<LoraStatus> status = orders.findLoraStatus(orderId);
        if (status.filter(s -> s == LoraStatus.SUBMITTED).isPresent()) {
            telegramClient.sendMessage(chatId, "Обучение уже запущено ⏳ Я напишу, когда всё будет готово.").subscribe();
            return;
        }
        if (status.filter(s -> s == LoraStatus.READY).isPresent()) {
            telegramClient.sendMessage(chatId, "Модель уже обучена! Введите свой промпт или выберите стиль 👇",
                            buildStyleKeyboard(orderId))
                    .subscribe();
            return;
        }

        telegramClient.sendMessage(chatId, "Запускаю обучение вашей персональной модели… 🚀")
                .onErrorResume(e -> {
                    log.warn("Failed to send 'starting training' message for order {}", orderId, e);
                    return Mono.empty();
                })
                .subscribe();

        loraTrainerService.trainAndPersist(orderId)
                .doOnSubscribe(sub -> telegramClient.sendMessage(
                                        chatId,
                                        "Обучение запущено🥳 \nОбычно это ≈ %d мин. Я напишу, когда модель будет готова😊"
                                                .formatted(trainingEtaMinutes)
                                )
                                .onErrorResume(e -> {
                                    log.warn("Failed to send ETA message for order {}", orderId, e);
                                    return Mono.empty();
                                })
                                .subscribe()
                )
                .flatMap(result -> telegramClient.sendMessage(
                                        chatId,
                                        "🎉 Обучение завершено! Модель готова.\nВведите промпт или выберите стиль 👇",
                                        buildStyleKeyboard(orderId)
                                )
                                .onErrorResume(e -> {
                                    log.warn("Failed to send 'training finished' message for order {}", orderId, e);
                                    return Mono.empty();
                                })
                )
                .doOnError(e -> {
                    log.warn("LoRA training failed for order {}", orderId, e);
                    telegramClient.sendMessage(chatId, "Не удалось завершить обучение. Попробуйте позже.")
                            .onErrorResume(e2 -> Mono.empty())
                            .subscribe();
                })
                .subscribe();
    }

    private void handleStyleCallback(Long chatId, String data) {
        String[] parts = data.split("\\|");
        if (parts.length < 3) {
            telegramClient.sendMessage(chatId, "Некорректный стиль. Попробуйте снова.").subscribe();
            return;
        }

        UUID orderId = parseOrderId(parts[1]);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Заказ не найден. Попробуйте позже.").subscribe();
            return;
        }

        if (isPhotoLimitReached(orderId)) {
            sendBuyMorePhotosMessage(chatId, orderId);
            return;
        }

        String styleKey = parts[2];

        var genderOpt = orders.findGender(orderId);
        if (genderOpt.isEmpty()) {
            telegramClient.sendMessage(chatId, "Не найден пол модели. Начните заново через /start.").subscribe();
            return;
        }
        Gender gender = genderOpt.get();

        String prompt;
        try {
            prompt = Styles.prompt(styleKey, gender);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown style {} or gender {} for order {}", styleKey, gender, orderId, ex);
            telegramClient.sendMessage(chatId, "Стиль недоступен. Выберите другой.").subscribe();
            return;
        }

        String styleLabel = Styles.labels().getOrDefault(styleKey, styleKey);
        generateAndSendImages(orderId, chatId, prompt, styleLabel);
    }

    private UUID parseOrderId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid order id received: {}", raw, ex);
            return null;
        }
    }

    private void handleGenderCallback(Long chatId, String payload) {
        String[] parts = payload.split("\\|");
        if (parts.length < 2) {
            telegramClient.sendMessage(chatId, "Некорректный выбор пола. Попробуйте снова.").subscribe();
            return;
        }
        UUID orderId = parseOrderId(parts[0]);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Заказ не найден. Попробуйте позже.").subscribe();
            return;
        }
        Gender gender;
        try {
            gender = Gender.valueOf(parts[1]);
        } catch (IllegalArgumentException ex) {
            telegramClient.sendMessage(chatId, "Не удалось распознать пол.").subscribe();
            return;
        }

        orders.setGender(orderId, gender);
        telegramClient.sendMessage(chatId, "Пол подтверждён: *%s*".formatted(genderLabel(gender))).subscribe();

        suggestTrainingIfEnoughPhotos(chatId, orderId);
    }

    private void generateAndSendImages(UUID orderId, long chatId, String prompt, String label) {
        log.error("GEN_CHECK orderId={}, uploadsCount={}", orderId, uploadService.countUploads(orderId));

        Optional<Gender> genderOpt = orders.findGender(orderId);
        if (genderOpt.isPresent()) {
            String trigger = switch (genderOpt.get()) {
                case FEMALE -> "sks woman";
                case MALE -> "zlk man";
            };
            String lowerPrompt = prompt.toLowerCase();
            if (!lowerPrompt.contains(trigger.toLowerCase())) {
                prompt = trigger + ", " + prompt;
            }
        }
        final String finalPrompt = prompt;

        Optional<String> loraPathOpt = resolveLoraPath(orderId, chatId);
        if (loraPathOpt.isEmpty()) return;
        final String loraPath = loraPathOpt.get();

        final int desired = Math.max(photoGenService.imagesPerRequest(), 1);

        Mono.fromCallable(() -> {
                    for (int n = desired; n >= 1; n--) {
                        if (orderRepository.tryReservePhotos(orderId, n)) {
                            return n;
                        }
                    }
                    return 0;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(reservedCount -> {
                    if (reservedCount <= 0) {
                        sendBuyMorePhotosMessage(chatId, orderId);
                        return Mono.empty();
                    }

                    telegramClient.sendMessage(chatId, "Генерирую %d фото… ✨".formatted(reservedCount)).subscribe();

                    return Flux.range(0, reservedCount)
                            .concatMap(i -> {
                                long seed = ThreadLocalRandom.current().nextLong();
                                return photoGenService.generateOne(finalPrompt, loraPath, seed)
                                        .flatMap(bytes ->
                                                telegramClient.sendPhoto(
                                                        chatId,
                                                        bytes,
                                                        "%s · Фото %d/%d".formatted(label, i + 1, reservedCount)
                                                )
                                        );
                            })
                            .then();
                })
                .subscribe();
    }

    private Optional<String> resolveLoraPath(UUID orderId, long chatId) {
        Optional<LoraStatus> status = orders.findLoraStatus(orderId);
        if (status.isEmpty() || status.get() != LoraStatus.READY) {
            telegramClient.sendMessage(chatId, "Модель ещё не готова. Я сообщу, когда обучение завершится.").subscribe();
            return Optional.empty();
        }
        Optional<String> loraPath = orders.findLoraPath(orderId);
        if (loraPath.isEmpty()) {
            telegramClient.sendMessage(chatId, "Не удалось найти обученную модель. Попробуйте позже.").subscribe();
        }
        return loraPath;
    }

    private Map<String, Object> buildStyleKeyboard(UUID orderId) {
        List<List<Map<String, Object>>> rows = new ArrayList<>();

        for (Map.Entry<String, String> e : Styles.labels().entrySet()) {
            String key = e.getKey();
            String label = e.getValue();
            rows.add(List.of(button(label, "STYLE|" + orderId + "|" + key)));
        }

        rows.add(List.of(button("📝 Свой промпт", "PROMPT|" + orderId)));
        rows.add(List.of(button("🔁 Переобучить модель", "RETRAIN_PREPAY|" + orderId)));

        return inlineKeyboard(rows);
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

    private Map<String, Object> genderKeyboard(UUID orderId) {
        return inlineKeyboard(List.of(List.of(
                button(genderLabel(Gender.MALE), "GENDER|" + orderId + "|MALE"),
                button(genderLabel(Gender.FEMALE), "GENDER|" + orderId + "|FEMALE")
        )));
    }

    private String genderLabel(Gender gender) {
        return switch (gender) {
            case MALE -> "♂️ Мужчина";
            case FEMALE -> "♀️ Женщина";
        };
    }

    // ===== Приём медиа =====

    @SuppressWarnings("unchecked")
    private boolean handleMediaUploads(Map<String, Object> message, Long chatId, Long telegramId) {
        List<Map<String, Object>> photos =
                Optional.ofNullable((List<Map<String, Object>>) message.get("photo")).orElse(List.of());
        Map<String, Object> document =
                Optional.ofNullable((Map<String, Object>) message.get("document")).orElse(null);

        if (photos.isEmpty() && document == null) return false;

        Optional<Order> maybeOrder = orders.findLatestOrderForTelegramUser(telegramId);
        if (maybeOrder.isEmpty()) {
            telegramClient.sendMessage(chatId, "Сначала создайте заказ командой */start*.").subscribe();
            return true;
        }
        UUID orderId = maybeOrder.get().id();

        if (!photos.isEmpty()) {
            Map<String, Object> largest = photos.stream()
                    .max(Comparator.comparingInt(o -> ((Number) o.getOrDefault("file_size", 0)).intValue()))
                    .orElse(photos.get(0));
            String fileId = String.valueOf(largest.get("file_id"));

            try {
                String url = telegramClient.getFileUrl(fileId).block();
                uploadService.saveUrl(orderId, url);
            } catch (Exception e) {
                log.warn("Failed to save photo url for order {} (fileId={})", orderId, fileId, e);
                telegramClient.sendMessage(chatId, "Не получилось принять фото. Отправьте ещё раз.").subscribe();
            }
        }

        if (document != null) {
            String fileId = String.valueOf(document.get("file_id"));
            try {
                String url = telegramClient.getFileUrl(fileId).block();
                uploadService.saveUrl(orderId, url);
            } catch (Exception e) {
                log.warn("Failed to save doc url for order {} (fileId={})", orderId, fileId, e);
                telegramClient.sendMessage(chatId, "Не получилось принять файл. Отправьте ещё раз.").subscribe();
            }
        }

        suggestTrainingIfEnoughPhotos(chatId, orderId);
        return true;
    }

    private void suggestTrainingIfEnoughPhotos(Long chatId, UUID orderId) {
        try {
            int count = uploadService.listUploads(orderId).size();

            telegramClient.sendMessage(chatId, "Фото получено ✅ (%d/%d)".formatted(count, minPhotos)).subscribe();

            if (count < minPhotos) return;

            if (count == minPhotos && orders.findGender(orderId).isEmpty()) {
                telegramClient.sendMessage(chatId, "Перед запуском обучения подтвердите пол модели:", genderKeyboard(orderId)).subscribe();
                return;
            }

            if (orders.findGender(orderId).isPresent()) {
                telegramClient.sendMessage(
                        chatId,
                        "Можно запускать обучение 🚀",
                        inlineKeyboard(List.of(List.of(button("Начать обучение", "TRAIN|" + orderId))))
                ).subscribe();
            }

        } catch (RuntimeException ex) {
            log.warn("Failed to fetch uploads for order {}", orderId, ex);
        }
    }

    private void sendReadyUI(Long chatId, UUID orderId) {
        telegramClient.sendMessage(
                chatId,
                "🎉 Ваша персональная модель уже обучена!\nМожете ввести свой промпт или выбрать стиль 👇",
                buildStyleKeyboard(orderId)
        ).subscribe();
    }

    // === лимит по данным заказа ===
    private boolean isPhotoLimitReached(UUID orderId) {
        try {
            int used = orders.getUsedPhotos(orderId);
            int limit = orders.getPhotosLimit(orderId);
            return used >= limit;
        } catch (RuntimeException ex) {
            log.warn("Failed to read photo limits for order {} when checking limit", orderId, ex);
            return false;
        }
    }

    private void sendBuyMorePhotosMessage(Long chatId, UUID orderId) {
        int limit;
        try {
            limit = orders.getPhotosLimit(orderId);
        } catch (RuntimeException ex) {
            log.warn("Failed to read photosLimit for order {} in sendBuyMorePhotosMessage, fallback to default", orderId, ex);
            limit = MAX_GENERATED_PHOTOS;
        }

        String text = """
                Лимит фото по этому заказу исчерпан — %d шт. 📸
                
                Чтобы сгенерировать больше снимков, докупите дополнительный пакет.
                """.formatted(limit);

        Map<String, Object> keyboard = inlineKeyboard(List.of(List.of(
                button("Докупить ещё фото", "BUY_EXTRA|" + orderId)
        )));

        telegramClient.sendMessage(chatId, text, keyboard).subscribe();
    }

    private void handleBuyExtraCallback(Long chatId, String orderIdRaw) {
        UUID orderId = parseOrderId(orderIdRaw);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Не удалось определить заказ. Попробуйте позже.").subscribe();
            return;
        }

        telegramClient.sendInvoice(
                        chatId,
                        "Дополнительные 60 фото",
                        "Продолжение генерации по текущей модели",
                        "EXTRA|" + orderId,
                        providerToken,
                        "RUB",
                        List.of(priceItem("Дополнительный пакет фото", PRICE_EXTRA_RUB))
                )
                .doOnError(e -> log.warn("Failed to send EXTRA invoice for order {}", orderId, e))
                .subscribe();
    }

    // === pre_checkout_query ===
    @SuppressWarnings("unchecked")
    private void handlePreCheckoutQuery(Map<String, Object> pre) {
        String id = Optional.ofNullable(pre.get("id")).map(Object::toString).orElse(null);
        if (id == null) return;

        telegramClient.answerPreCheckoutQuery(id, true, null)
                .doOnError(e -> log.warn("Failed to answer pre_checkout_query {}", id, e))
                .subscribe();
    }

    // === successful_payment ===
    @SuppressWarnings("unchecked")
    private void handleSuccessfulPayment(Long chatId, Long telegramId, Map<String, Object> successfulPayment) {
        String payload = Optional.ofNullable(successfulPayment.get("invoice_payload"))
                .map(Object::toString)
                .orElse(null);

        if (payload == null) {
            log.warn("successful_payment without payload: {}", successfulPayment);
            return;
        }

        log.info("Successful payment with payload={}", payload);

        if (payload.startsWith("MAIN|")) {
            UUID orderId = parseOrderId(payload.substring("MAIN|".length()));
            if (orderId == null) return;
            handleMainPaymentSuccess(chatId, orderId);
            return;
        }

        if (payload.startsWith("RETRAIN|")) {
            UUID orderId = parseOrderId(payload.substring("RETRAIN|".length()));
            if (orderId == null) return;
            handleRetrainPaymentSuccess(chatId, telegramId, orderId);
            return;
        }

        if (payload.startsWith("EXTRA|")) {
            UUID orderId = parseOrderId(payload.substring("EXTRA|".length()));
            if (orderId == null) return;
            handleExtraPackPaymentSuccess(chatId, orderId);
            return;
        }

        if (payload.startsWith(PAYLOAD_FOLLOWUP_30)) {
            UUID orderId = parseOrderId(payload.substring(PAYLOAD_FOLLOWUP_30.length()));
            if (orderId == null) return;
            handleFollowupPaymentSuccess(chatId, orderId);
            return;
        }

        if (payload.startsWith(PAYLOAD_FOLLOWUP_24)) {
            UUID orderId = parseOrderId(payload.substring(PAYLOAD_FOLLOWUP_24.length()));
            if (orderId == null) return;
            handleFollowupPaymentSuccess(chatId, orderId);
        }
    }

    private void handleMainPaymentSuccess(Long chatId, UUID orderId) {
        try {
            orders.markPaid(orderId);
            orderRepository.markPurchased(orderId, Instant.now());
        } catch (RuntimeException ex) {
            log.warn("Failed to mark order {} as paid (MAIN)", orderId, ex);
            telegramClient.sendMessage(chatId, "Не удалось принять оплату. Попробуйте позже.").subscribe();
            return;
        }

        telegramClient.sendMessage(
                chatId,
                "Оплата принята ✅ Шаг 2/3 — загрузите *%d–%d фото* для обучения."
                        .formatted(minPhotos, maxPhotos)
        ).subscribe();
    }

    // === успех оплаты добивок: тот же UX что MAIN ===
    private void handleFollowupPaymentSuccess(Long chatId, UUID orderId) {
        try {
            orders.markPaid(orderId);
            orderRepository.markPurchased(orderId, Instant.now());
        } catch (RuntimeException ex) {
            log.warn("Failed to mark order {} as paid (FOLLOWUP)", orderId, ex);
            telegramClient.sendMessage(chatId, "Не удалось принять оплату. Попробуйте позже.").subscribe();
            return;
        }

        telegramClient.sendMessage(
                chatId,
                "Оплата принята ✅ Шаг 2/3 — загрузите *%d–%d фото* для обучения."
                        .formatted(minPhotos, maxPhotos)
        ).subscribe();
    }

    private void handleRetrainPaymentSuccess(Long chatId, Long telegramId, UUID orderId) {
        try {
            orders.markPaid(orderId);
        } catch (RuntimeException ex) {
            log.warn("Failed to mark retrain order {} as paid", orderId, ex);
            telegramClient.sendMessage(chatId, "Не удалось принять оплату. Попробуйте позже.").subscribe();
            return;
        }

        try {
            orders.purgeUserDataForRetrain(telegramId, orderId);
        } catch (RuntimeException ex) {
            log.warn("Failed to purge user data for retrain [telegramId={}, orderId={}]", telegramId, orderId, ex);
        }

        telegramClient.sendMessage(
                chatId,
                ("Оплата за переобучение принята ✅\nШаг 2/3 — загрузите *%d–%d фото* для переобучения.\n\n" +
                        "\uD83D\uDCF8 Какие фото нужны:\n" +
                        "• 10–30 ваших фото хорошего качества.\n" +
                        "• Разные: портреты, по пояс и в полный рост.\n" +
                        "• Лицо видно: анфас, 3/4, профиль.\n" +
                        "• Разное освещение, ракурсы и одежда.\n" +
                        "• Без фильтров/масок/очков/сильных теней.\n" +
                        "• Только вы на фото.\n\n" +
                        "Разнообразные фото дают самую точную и красивую модель ✨ ")
                        .formatted(minPhotos, maxPhotos)
        ).subscribe();
    }

    private void handleExtraPackPaymentSuccess(Long chatId, UUID orderId) {
        try {
            orders.increasePhotosLimit(orderId, MAX_GENERATED_PHOTOS);
        } catch (RuntimeException ex) {
            log.warn("Failed to increase photosLimit for order {} after EXTRA payment", orderId, ex);
            telegramClient.sendMessage(chatId,
                    "Оплата прошла, но не удалось обновить лимит фото. Напишите в поддержку, мы разберёмся 🙏"
            ).subscribe();
            return;
        }

        telegramClient.sendMessage(
                chatId,
                "Оплата за дополнительный пакет принята ✅\nТеперь вы можете сгенерировать ещё %d фото по этому заказу 🎉"
                        .formatted(MAX_GENERATED_PHOTOS)
        ).subscribe();
    }

    // =====================================================================
    // НОВОЕ: обработка причин (FOLLOWUP_REASON|REASON|orderId)
    // =====================================================================
    private void handleFollowupReason(Long chatId, String data) {
        String[] parts = data.split("\\|");
        if (parts.length < 3) {
            telegramClient.sendMessage(chatId, "Не удалось обработать выбор.").subscribe();
            return;
        }

        String reason = parts[1];
        UUID orderId = parseOrderId(parts[2]);
        if (orderId == null) {
            telegramClient.sendMessage(chatId, "Заказ не найден.").subscribe();
            return;
        }

        String text = switch (reason) {
            case "PRICE_HIGH" -> FOLLOWUP_24H_ANSWER_PRICE.formatted(PRICE_FOLLOWUP_24_RUB);
            case "QUALITY" -> FOLLOWUP_24H_ANSWER_QUALITY.formatted(PRICE_FOLLOWUP_24_RUB);
            case "PRIVACY" -> FOLLOWUP_24H_ANSWER_PRIVACY.formatted(PRICE_FOLLOWUP_24_RUB);
            case "NO_NEED" -> FOLLOWUP_24H_ANSWER_NO_NEED.formatted(PRICE_FOLLOWUP_24_RUB);
            case "OTHER" -> FOLLOWUP_24H_ANSWER_OTHER.formatted(PRICE_FOLLOWUP_24_RUB);
            default -> "Принял! Спасибо 🙌";
        };

        Map<String, Object> buyKb = inlineKeyboard(List.of(List.of(
                button("1 модель и 18 фото | " + PRICE_FOLLOWUP_24_RUB + "₽", "BUY_FOLLOWUP_24|" + orderId)
        )));

        telegramClient.sendMessage(chatId, text, buyKb).subscribe();
    }
}