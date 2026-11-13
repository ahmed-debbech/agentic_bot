package com.debbech.rest;

import com.debbech.ai.LLMService;
import com.debbech.rest.models.ChatRequest;
import com.debbech.rest.models.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final LLMService chatService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        //String res = chatService.chat(request.message());
        return null;//new ChatResponse(request.message(), res, chatService.getConversation());
    }
}
