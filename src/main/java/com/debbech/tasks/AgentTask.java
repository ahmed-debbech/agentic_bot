package com.debbech.tasks;

import com.debbech.tasks.hackernews.exceptions.EndLoopException;

public interface AgentTask {
    String behaveOnAiResponse(String aiResponse) throws EndLoopException;
    String initialPrompt() throws Exception;
}
