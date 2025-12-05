package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.openai.OpenAiChatModel;

@Service
public class AiService {
    private final OpenAiChatModel model;

    public AiService(@Value("${openai.api.key}") String apiKey) {
        this.model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .build();
    }

    public String ask(String question) {
        return model.generate(question);
    }
}