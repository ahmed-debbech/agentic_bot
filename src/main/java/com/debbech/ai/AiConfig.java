package com.debbech.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@Getter
public class AiConfig {

    @Value("${app.ai.model}")
    private String model;

    @Value("${app.ai.secret}")
    private String secret;

    @Value("${app.ai.url}")
    private String url;

    private OpenAiChatModel chatModel;

    @Bean
    public ChatModel chatModel() {

        chatModel = OpenAiChatModel.builder()
                .baseUrl(url)
                .apiKey(secret)
                .modelName(model)
                .build();

        return chatModel;
    }
}
