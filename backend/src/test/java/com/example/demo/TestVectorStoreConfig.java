package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

@Profile("test")
@Configuration
public class TestVectorStoreConfig {

    @Bean
    public EmbeddingStore vectorStoreTest() {
        return new InMemoryEmbeddingStore<>();
    }
}
