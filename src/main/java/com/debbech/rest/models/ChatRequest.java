package com.debbech.rest.models;

public record ChatRequest(
        String conversationId,
        String message
) {}