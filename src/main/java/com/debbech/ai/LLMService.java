package com.debbech.ai;

import com.debbech.store.Actor;
import com.debbech.store.ConversationStore;
import com.debbech.tasks.AgentTask;
import com.debbech.tasks.hackernews.HackerNewsTask;
import com.debbech.tasks.hackernews.exceptions.EndLoopException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class LLMService {

    @Autowired
    private AiResourcesManager resourcesManager;

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private ConversationStore conversationStore;

    private MessageWindowChatMemory memory;

    public void executeAgentLoop(AgentTask agentTask){

        String convId = conversationStore.createConversation();

        this.memory = MessageWindowChatMemory.withMaxMessages(20);
        int iteration = 1;

        log.info("SYSTEM is writing---");
        if(!resourcesManager.getSystemPrompt().isEmpty()){
            memory.add(UserMessage.from(resourcesManager.getSystemPrompt()));
            conversationStore.appendMessage(convId, Actor.SYSTEM, resourcesManager.getSystemPrompt());
        }else{
            log.warn("No system prompt provided.");
        }
        log.info("{} iterations", iteration);

        String promptInit = null;
        try {
            promptInit = agentTask.initialPrompt();
        } catch (Exception e) {
            log.error("could not provide initial prompt to the LLM");
            throw new RuntimeException(e);
        }

        log.info("USER is writing initial prompt---");
        memory.add(UserMessage.from(
                promptInit
        ));
        conversationStore.appendMessage(convId, Actor.USER, promptInit);

        ChatResponse response = aiConfig.getChatModel().chat(memory.messages());
        log.info("ASSISTANT is writing---");
        memory.add(response.aiMessage());
        conversationStore.appendMessage(convId, Actor.ASSISTANT, response.aiMessage().text());

        String nextPrompt = "";
        try{
            nextPrompt = agentTask.behaveOnAiResponse(response.aiMessage().text());
            iteration++;
        }catch(EndLoopException ele){
            log.info("DONE---");
            conversationStore.appendMessage(convId, Actor.SYSTEM, "--DONE-- " + ele.getMessage());
            return;
        }

        while (true) {
            log.info("{} iterations", iteration);

            log.info("USER is writing---");
            memory.add(UserMessage.from(nextPrompt));
            conversationStore.appendMessage(convId, Actor.USER, nextPrompt);

            ChatResponse response1 = aiConfig.getChatModel().chat(memory.messages());

            log.info("ASSISTANT is writing---");
            AiMessage assistantMessage = response1.aiMessage();
            memory.add(assistantMessage);
            conversationStore.appendMessage(convId, Actor.ASSISTANT, assistantMessage.text());

            try {
                nextPrompt = agentTask.behaveOnAiResponse(response1.aiMessage().text());
            }catch(EndLoopException ele){
                log.info("DONE---");
                conversationStore.appendMessage(convId, Actor.SYSTEM, "--DONE-- " + ele.getMessage());
                break;
            }
            iteration++;
        }

    }
}
