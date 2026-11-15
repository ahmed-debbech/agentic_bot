package com.debbech.agenticbot.sched;

import com.debbech.agenticbot.ai.LLMService;
import com.debbech.agenticbot.notification.NotificationService;
import com.debbech.agenticbot.tasks.hackernews.HackerNewsTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class Cron {

    @Autowired
    private LLMService llmService;
    
    @Autowired(required = false)
    private NotificationService notificationService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(cron = "0 0 8,14,20 * * ?")
    public void executeHackerNews() {
        executeHackerNewsJob();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStartup() {
        executeHackerNewsJob();
    }

    private void executeHackerNewsJob() {
        if (!running.compareAndSet(false, true)) {
            return; // Already running, skip this execution
        }
        try {
            String aiResponse = llmService.executeAgentLoop(new HackerNewsTask());
            if (notificationService != null) {
                notificationService.sendMessage(aiResponse)
                    .thenAccept(sent -> {
                        if (sent) {
                            System.out.println("Successfully sent notification");
                        } else {
                            System.out.println("Failed to send notification");
                        }
                    });
            }
        } finally {
            running.set(false);
        }
    }
}
