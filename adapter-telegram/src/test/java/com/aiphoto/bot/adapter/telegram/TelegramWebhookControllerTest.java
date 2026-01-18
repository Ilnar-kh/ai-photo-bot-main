//package com.aiphoto.bot.adapter.telegram;
//
//import com.aiphoto.bot.adapter.telegram.controller.TelegramWebhookController;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//class TelegramWebhookControllerTest {
//
//    private final MockMvc mvc = MockMvcBuilders
//            .standaloneSetup(new TelegramWebhookController())
//            .alwaysDo(print())
//            .build();
//
//    @Test
//    void acceptsWebhook() throws Exception {
//        mvc.perform(post("/webhook/telegram")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {"update_id":1,"message":{"text":"hello"}}"""))
//                .andExpect(status().isOk());
//    }
//}