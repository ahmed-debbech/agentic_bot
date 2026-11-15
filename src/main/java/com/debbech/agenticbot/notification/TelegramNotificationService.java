package com.debbech.agenticbot.notification;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending notifications to a Telegram channel via a bot.
 */
@Service
@Slf4j
public class TelegramNotificationService implements NotificationService {

    @Value("${telegram.chat.token}")
    private String botToken;
    @Value("${telegram.chat.id")
    private String chatId;

    private HttpClient httpClient;

    
    @Override
    public CompletableFuture<Boolean> sendRecommendedArticles(List<Article> articles) {
        if (articles.isEmpty()) {
            // Send a message indicating no articles were recommended
            return sendMessage("Recommended HN posts: \nNothing is recommended");
        }
        
        StringBuilder messageBuilder = new StringBuilder("Recommended HN posts: \n");
        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            messageBuilder.append(i + 1).append(". ").append(article.getTitle())
                    .append("\n   Link: ").append(article.getUrl());
            if (article.getSummary() != null && !article.getSummary().isEmpty()) {
                messageBuilder.append("\n   Summary: ").append(article.getSummary());
            }
            messageBuilder.append("\n\n");
        }
        
        return sendMessage(messageBuilder.toString().trim());
    }
    
    /**
     * Sends a message to the configured Telegram channel.
     *
     * @param message The message to send
     * @return CompletableFuture indicating success or failure
     */
    public CompletableFuture<Boolean> sendMessage(String message) {
        try {
            String encodedMessage = java.net.URLEncoder.encode(message, StandardCharsets.UTF_8);
            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "chat_id=" + chatId + "&text=" + encodedMessage))
                    .build();
            
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            log.info("Successfully sent message to Telegram channel");
                            return true;
                        } else {
                            log.error("Failed to send message to Telegram. Status: {}, Response: {}",
                                    response.statusCode(), response.body());
                            return false;
                        }
                    })
                    .exceptionally(throwable -> {
                        log.error("Exception occurred while sending message to Telegram", throwable);
                        return false;
                    });
        } catch (Exception e) {
            log.error("Error preparing Telegram message", e);
            return CompletableFuture.completedFuture(false);
        }
    }
}