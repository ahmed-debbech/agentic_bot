package com.debbech.agenticbot.ai;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class AiResourcesManager {

    private String systemPrompt;

    @PostConstruct
    public void init(){
        String content = null;

        try {
            content = Files.readString(
                    Path.of("/prompts/system_prompt.md"),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            log.error("NO system prompt found to begin with.");
            throw new RuntimeException(e);
        }
        this.systemPrompt = content;
    }

    public String getSystemPrompt(){
        return this.systemPrompt;
    }

}
