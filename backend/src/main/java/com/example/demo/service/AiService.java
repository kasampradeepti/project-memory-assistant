package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;

@Service
public class AiService {

    private final OpenAiChatModel model;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    public AiService(@Value("${openai.api.key}") String apiKey) {
        this.model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .build();
    }

    public String ask(String question) {

        // 1. Create embedding for question
        Embedding queryEmbedding = embeddingModel.embed(question).content();

        //2. Retrieve matching TextSegments
        List<EmbeddingMatch<TextSegment>> matches =
                embeddingStore.findRelevant(queryEmbedding, 3);
        System.out.println("Matches found = " + matches.size());
        //3. Extract actual TEXT safely
        StringBuilder context = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : matches) {
            context.append(match.embedded().text()).append("\n");
            System.out.println("Match chunk: " + match.embedded().text());
        }

        //4. Build RAG prompt
        //old prompt - works well strictly within context
        //  You are a document-based AI assistant.
        // You MUST answer ONLY using the context below.
        // If the answer is not present in the context, reply exactly:
        // "Information not available in the uploaded document."
        String prompt = """
        You are a helpful AI assistant.
        Use the document context when relevant.
        If the question is not covered by the document, you may answer using your general knowledge.

        Context:
        %s

        User Question:
        %s
        """.formatted(context.toString(), question);

        //5. Ask LLM
        return model.generate(prompt);
    }
}
