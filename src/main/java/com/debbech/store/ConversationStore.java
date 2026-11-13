package com.debbech.store;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class ConversationStore {

    @Value("${app.conversations.path}")
    private String conversationsPath;

    public String createConversation() {
        String utcTime = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        log.info("creating conversation with name conv_{}", utcTime);
        return utcTime;
    }

    public void appendMessage(String convId, Actor actor, String content) {

        Path path = Path.of(conversationsPath+"/conv_"+convId);

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            writer.newLine();
            writer.newLine();
            writer.write("[[[" + actor.toString() + "("+DateTimeFormatter.ISO_INSTANT.format(Instant.now())+") ]]]: " + content);
            writer.newLine();
        }catch (IOException e){
            log.warn("could not store message in conversation {}", convId);
        }
    }
}
