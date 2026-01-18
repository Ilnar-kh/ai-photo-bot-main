package com.aiphoto.bot.core.service;

import com.aiphoto.bot.core.domain.Gender;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Styles {

    // Ключи стилей (используются в callback-data)
    public static final String STYLE_FOCUS       = "focus";
    public static final String STYLE_SHAPE       = "shape";
    public static final String STYLE_BLOOM       = "bloom";
    public static final String STYLE_STAGE       = "stage";
    public static final String STYLE_FIELD       = "field";
    public static final String STYLE_MONO        = "mono";
    public static final String STYLE_MONO_STREET = "mono_street";

    /**
     * Текст для кнопок (готово под Telegram):
     * key -> "📸 Focus\nПортрет крупным планом"
     */
    private static final Map<String, String> LABELS;

    /**
     * Промты для женских фото.
     */
    private static final Map<String, String> FEMALE_PROMPTS;

    /**
     * Промты для мужских фото.
     */
    private static final Map<String, String> MALE_PROMPTS;

    static {
        // ---------- КНОПКИ (название + краткое описание) ----------
        Map<String, String> labels = new LinkedHashMap<>();

        labels.put(STYLE_FOCUS,
                "📸 Focus - портрет");

        labels.put(STYLE_SHAPE,
                "📐 Shape - полный рост");

        labels.put(STYLE_BLOOM,
                "🌼 Bloom - с цветами, мягкие тона");

        labels.put(STYLE_STAGE,
                "🪑 Stage - образ со стулом");

        labels.put(STYLE_FIELD,
                "🌾 Field - поле, ветер, простор");

        labels.put(STYLE_MONO,
                "⚫ Mono - Ч/Б студия");

        labels.put(STYLE_MONO_STREET,
                "🏙️ Mono Street - Ч/Б город");

        LABELS = Collections.unmodifiableMap(labels);

        // ---------- ЖЕНСКИЕ ПРОМТЫ ----------
        Map<String, String> female = new LinkedHashMap<>();

        // Focus -> Студия 1 — жен
        female.put(STYLE_FOCUS,
                "Hyper-realistic 8K editorial frame. Close-up studio portrait. Studio shoot with dramatic spotlight vignette similar. " +
                        "Cinematic contrast, deep shadows, warm highlights on skin. Atmospheric mood: intimate editorial glamour. " +
                        "Female model wearing a black fitted bandeau top, open oversized black suit jacket sliding off shoulders. " +
                        "Fabric surface detailed: wool texture, soft gloss. Hairstyle: loose voluminous waves, slightly messy, catching the light. " +
                        "Makeup: bronzed skin, defined brows, smoky eyeliner, soft matte nude lips. " +
                        "Expression: intense confident stare directly into the camera, raw emotion, subtle smirk. " +
                        "Keep original facial features from the uploaded photo. Shot on 85mm lens, low depth of field."
        );

        // Shape -> Студия 2 — жен
        female.put(STYLE_SHAPE,
                "Hyper-realistic 8K editorial frame. A studio backdrop with a centered spotlight vignette, warm cinematic lighting, and soft but directional shadows. " +
                        "Women model in black bandeau top, oversized black suit jacket, tailored trousers, black heels. " +
                        "Pose: standing with legs apart slightly, arms crossed or holding jacket closed at waist. Presence strong and stylish. " +
                        "Expression: serious fashion look, subtle attitude. Atmosphere: Vogue editorial power shot. " +
                        "Keep original face. Shot on 35mm lens."
        );

        // Bloom -> Студия 3 — жен
        female.put(STYLE_BLOOM,
                "Hyper-realistic 8K, ultra-high-resolution studio portrait of a young woman holding a bouquet of wilted autumn chrysanthemums and roses. " +
                        "Warm tungsten key light from the left, deep cinematic shadows on the right. Hyper-realistic skin texture, soft matte finish, visible pores. " +
                        "Natural wavy hair resting on her shoulders. Expression: quiet melancholy, distant gaze to the side. " +
                        "Outfit: muted brown blazer with detailed fabric texture. Atmosphere: nostalgic autumn mood, painterly tones, moody silence. " +
                        "Keep original face from uploaded photo."
        );

        // Stage -> Студия 4 — жен
        female.put(STYLE_STAGE,
                "Hyper-realistic 8K fashion portrait of a woman sitting sideways on an old wooden chair, arms wrapped loosely around her torso. " +
                        "Sharp skin detail with natural texture, peach fuzz, realistic gloss highlights. " +
                        "Lighting: single dramatic spotlight cutting across her face, harsh shadows behind. " +
                        "Outfit: dark green vintage blouse with textured fabric folds. " +
                        "Expression: raw vulnerability, thoughtful, slightly sad. " +
                        "Atmosphere: theatrical, intimate, dusty old studio. Keep original face from uploaded photo."
        );

        // Field -> Поле 6 — жен
        female.put(STYLE_FIELD,
                "Hyper-realistic 8K editorial frame, golden prairie, cinematic sunlight, soft breeze moving ringlets, warm-glow skin, " +
                        "flowy linen dress with delicate embroidery, model standing, one hand gently lifting hair, soft smile and dreamy gaze, " +
                        "editorial feminine elegance, cinematic shadows creating depth, Vogue/Bazaar editorial fashion energy. Shot on 90mm lens."
        );

        // Mono -> Студия 7 ч/б — жен
        female.put(STYLE_MONO,
                "Use subject’s face. Black and white studio portrait, the woman with softly curled hair sitting gracefully on the floor, " +
                        "leaning on one arm while her other hand rests lightly on her waist. She wears a black silk slip dress with thin straps, " +
                        "her gaze calm and confident, body slightly turned, soft light highlighting the gentle shine of the silk and the texture of her curls, " +
                        "elegant and cinematic composition."
        );

        // Mono Street -> Улица чб 8 — жен
        female.put(STYLE_MONO_STREET,
                "Use subject’s face. Hyper-realistic 8K editorial frame, black-and-white mid-shot of model leaning casually against a New York streetlight or traffic pole, " +
                        "confident and slightly aloof expression in the style of Vogue, black tailored coat draped elegantly, white shirt and black tie neatly styled, " +
                        "black mini skirt and stiletto heels, oversized sunglasses, cinematic urban sunlight casting dramatic shadows across textures, " +
                        "blurred city background, Vogue-style storytelling, chic, high-fashion street energy, subtle motion for realism."
        );

        FEMALE_PROMPTS = Collections.unmodifiableMap(female);

        // ---------- МУЖСКИЕ ПРОМТЫ ----------
        Map<String, String> male = new LinkedHashMap<>();

        // Focus -> Студия 1 — муж
        male.put(STYLE_FOCUS,
                "Hyperrealistic high-end fashion portrait, ultra-sharp. Skin: hyperrealistic skin texture, detailed pores, cool glossy highlights. " +
                        "Clothing: black minimalistic blazer with structured shoulders, realistic wool texture. Hair: sleek wet-look styling. " +
                        "Expression: cold fashion stare, emotionless elegance. Lighting: blue gel side-light + neutral key light, glossy reflections on cheekbones. " +
                        "Atmosphere: high-fashion studio set, slight soft haze behind. POV: close portrait, centered. Use original face from the uploaded photo."
        );

        // Shape -> Студия 2 — муж
        male.put(STYLE_SHAPE,
                "Hyper-realistic 8K editorial frame. Studio minimal backdrop. " +
                        "Lighting: soft diffused studio light with gentle sculpting shadows, subtle warm highlights, light cinematic gradient on the wall. " +
                        "Male model sitting relaxed on the floor in front of a soft modern lounge chair. " +
                        "Clothing: oversized knit sweater, relaxed straight-fit jeans, casual worn-in shoes. " +
                        "Mood: laid-back masculine confidence, effortless charm. Pose: leaning back on hands, natural slump. " +
                        "Expression: wide carefree smile. Atmosphere: quiet studio intimacy, warm aesthetic. Keep original face. Shot on 50mm lens."
        );

        // Bloom -> Студия 3 — муж
        male.put(STYLE_BLOOM,
                "Hyper-realistic studio portrait of a man holding a small bouquet of dried wildflowers. " +
                        "Warm tungsten key light from one side, deep cinematic shadows contouring the face. " +
                        "Skin: textured with natural pores, subtle beard stubble. Outfit: brown vintage jacket. " +
                        "Expression: reflective, quiet emotional weight. Atmosphere: moody melancholic autumn vignette. Keep original face."
        );

        // Stage -> Студия 4 — муж
        male.put(STYLE_STAGE,
                "Ultra-high-resolution portrait of a man sitting slightly hunched, hands clasped between his knees. " +
                        "Harsh golden side light casts long shadows, emphasizing strong jawline. " +
                        "Skin: hyper-realistic micro-details, matte texture. " +
                        "Outfit: dark shirt with visible linen texture. " +
                        "Expression: intense, introspective, raw. Atmosphere: cinematic stillness. Keep original face."
        );

        // Field -> Поле 6 — муж
        male.put(STYLE_FIELD,
                "Hyper-realistic 8K editorial frame, full-length in open prairie with tall grasses and wildflowers, cinematic sunset light, " +
                        "golden rays piercing clouds, gentle wind moving hair and clothing, tousled hairstyle, natural warm skin tones, focused serious gaze, " +
                        "linen-cotton shirt, brown leather vest, linen trousers, realistic folds, casual boots, Vogue editorial movement, confident stride, " +
                        "mood of free-spirited sophistication."
        );

        // Mono -> Студия 7 ч/б — муж
        male.put(STYLE_MONO,
                "Black and white studio shot of a man sitting sideways on a metal chair, leaning back dynamically, captured from a tilted low angle, " +
                        "open black suit jacket, unbuttoned white shirt, one leg slightly extended forward, cinematic shadow play on the floor, " +
                        "black background, avant-garde editorial tone, --ar 2:3 --v 6 --q 2 --style photographic --lighting dramatic."
        );

        // Mono Street -> Улица чб 8 — муж
        male.put(STYLE_MONO_STREET,
                "Hyper-realistic 8K editorial frame, black-and-white close-up portrait of model shot from slightly elevated angle over NYC streets, " +
                        "man’s sunglasses partially visible, ringlets framing face, black tailored coat and white shirt with black tie, " +
                        "cinematic sunlight creating dramatic shadows, wind lifting coat, blurred high-rise buildings and traffic below, realistic textures, " +
                        "Vogue-style storytelling, chic, dynamic, sophisticated."
        );

        MALE_PROMPTS = Collections.unmodifiableMap(male);
    }

    private Styles() {
    }

    /**
     * Текст для кнопок (с эмодзи и описанием).
     * Используется в buildStyleKeyboard().
     */
    public static Map<String, String> labels() {
        return LABELS;
    }

    /**
     * Получить промт по ключу стиля и полу.
     * Если пол не задан или не распознан — бросаем IllegalArgumentException.
     */
    public static String prompt(String styleKey, Gender gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Gender is null for style: " + styleKey);
        }

        Map<String, String> map = switch (gender) {
            case FEMALE -> FEMALE_PROMPTS;
            case MALE -> MALE_PROMPTS;
        };

        String prompt = map.get(styleKey);
        if (prompt == null) {
            throw new IllegalArgumentException("Unknown style key: " + styleKey + " for gender: " + gender);
        }
        return prompt;
    }
}