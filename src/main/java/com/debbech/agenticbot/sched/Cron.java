package com.debbech.agenticbot.sched;

import com.debbech.agenticbot.ai.LLMService;
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
            llmService.executeAgentLoop(new HackerNewsTask());
        } finally {
            running.set(false);
        }
    }
}
