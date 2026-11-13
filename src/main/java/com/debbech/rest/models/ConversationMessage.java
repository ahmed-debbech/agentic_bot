package com.debbech.rest.models;

public record ConversationMessage(
        String role,
        String content
) {}
