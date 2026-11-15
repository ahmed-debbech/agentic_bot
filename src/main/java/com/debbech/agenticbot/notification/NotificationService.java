package com.debbech.agenticbot.notification;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for sending notifications through various channels.
 */
public interface NotificationService {
    
    /**
     * Sends recommended articles to the notification channel.
     * 
     * @param articles List of recommended articles
     * @return CompletableFuture indicating success or failure
     */
    CompletableFuture<Boolean> sendRecommendedArticles(List<Article> articles);
    
    /**
     * Simple class to represent an article for notification purposes.
     */
    class Article {
        private final String title;
        private final String url;
        private final String summary;
        
        public Article(String title, String url, String summary) {
            this.title = title;
            this.url = url;
            this.summary = summary;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getUrl() {
            return url;
        }
        
        public String getSummary() {
            return summary;
        }
    }
}