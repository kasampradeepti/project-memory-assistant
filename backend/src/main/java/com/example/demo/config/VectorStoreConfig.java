package com.example.demo.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test") 
public class VectorStoreConfig {

    @Bean
    public EmbeddingStore<TextSegment> vectorStore() {
        return ChromaEmbeddingStore.builder()

        .baseUrl("http://127.0.0.1:8000") //http://localhost:8000
            .collectionName("project_docs")
            .build();
    }
}
