package com.debbech.rest.models;

import java.util.List;

public record ChatResponse(
        String userMessage,
        String assistantMessage,
        List<String> conversation
) {}