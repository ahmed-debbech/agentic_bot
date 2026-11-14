package com.debbech.ai;

import com.debbech.store.Actor;
import com.debbech.store.ConversationStore;
import com.debbech.tasks.AgentTask;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LLMService {

    @Autowired
    private AiResourcesManager resourcesManager;

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private ConversationStore conversationStore;

    public void executeAgentLoop(AgentTask agentTask) {

        String convId = conversationStore.createConversation();

        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(20);

        String systemPrompt = resourcesManager.getSystemPrompt();
        log.info("SYSTEM is writing---");
        if (!systemPrompt.isEmpty()) {
            conversationStore.appendMessage(convId, Actor.SYSTEM, systemPrompt);
        } else {
            log.warn("No system prompt provided.");
        }

        String promptInit;
        try {
            promptInit = agentTask.initialPrompt();
        } catch (Exception e) {
            log.error("could not provide initial prompt to the LLM");
            throw new RuntimeException(e);
        }

        log.info("USER is writing initial prompt---");
        conversationStore.appendMessage(convId, Actor.USER, promptInit);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(aiConfig.getChatModel())
                .chatMemory(memory)
                .systemMessageProvider(ignored -> systemPrompt)
                .tools(agentTask.tools()).maxToolCallingRoundTrips(15)
                .toolArgumentsErrorHandler(ToolArgumentsErrorHandler.sendExceptionMessageToLlm())
                .toolExecutionErrorHandler(ToolExecutionErrorHandler.failInvocationUnlessVisibleToLlm())
                .build();

        Result<String> result = assistant.chat(promptInit);

        for (ToolExecution toolExecution : result.toolExecutions()) {
            ToolExecutionRequest toolRequest = toolExecution.request();
            log.info("TOOL is called: {}", toolRequest.name());
            conversationStore.appendMessage(convId, Actor.TOOL,
                    toolRequest.name() + "(" + toolRequest.arguments() + ") -> " + toolExecution.result());
        }

        log.info("ASSISTANT is writing---");
        conversationStore.appendMessage(convId, Actor.ASSISTANT, result.content());
        log.info("DONE---");
    }
}
