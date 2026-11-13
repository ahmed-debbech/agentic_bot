package com.debbech.sched;

import com.debbech.ai.LLMService;
import com.debbech.tasks.hackernews.HackerNewsTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class Cron {

    @Autowired
    private LLMService llmService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(cron = "0 * * * * *")
    public void executeHackerNews() {

        if (!running.compareAndSet(false, true)) {
            return; // Already running, skip this execution
        }
        try {
            llmService.executeAgentLoop(new HackerNewsTask());
        }finally {
            running.set(false);
        }
    }
}
