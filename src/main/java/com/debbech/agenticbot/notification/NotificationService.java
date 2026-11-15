package com.debbech.agenticbot.notification;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for sending notifications through various channels.
 */
public interface NotificationService {
    
    /**
     * Sends a text message to the notification channel.
     * 
     * @param message The message to send
     * @return CompletableFuture indicating success or failure
     */
    CompletableFuture<Boolean> sendMessage(String message);
}