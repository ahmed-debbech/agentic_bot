package com.debbech.ai;

import dev.langchain4j.service.Result;

interface Assistant {
    Result<String> chat(String userMessage);
}
