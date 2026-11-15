package com.debbech.agenticbot.tasks;

import java.util.List;

public interface AgentTask {
    String initialPrompt() throws Exception;
    List<Object> tools();
}
