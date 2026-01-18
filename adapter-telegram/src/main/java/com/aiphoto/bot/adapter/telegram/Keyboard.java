package com.aiphoto.bot.adapter.telegram;

import java.util.List;
import java.util.Map;

public class Keyboard {

    public static Map<String, Object> reply(String buttonText) {
        return Map.of(
                "reply_markup", Map.of(
                        "keyboard", List.of(List.of(Map.of("text", buttonText))),
                        "resize_keyboard", true,
                        "one_time_keyboard", true
                )
        );
    }

    public record InlineBtn(String text, String data) {}

    public static Map<String, Object> inline(InlineBtn... buttons) {
        return Map.of(
                "reply_markup", Map.of(
                        "inline_keyboard", List.of(
                                List.of((Object[]) java.util.Arrays.stream(buttons)
                                        .map(b -> Map.of("text", b.text(), "callback_data", b.data()))
                                        .toArray())
                        )
                )
        );
    }
}