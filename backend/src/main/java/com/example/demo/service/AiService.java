// package com.example.demo.service;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;

// import dev.langchain4j.data.embedding.Embedding;
// import dev.langchain4j.data.segment.TextSegment;
// import dev.langchain4j.model.embedding.EmbeddingModel;
// import dev.langchain4j.model.openai.OpenAiChatModel;
// import dev.langchain4j.store.embedding.EmbeddingMatch;
// import dev.langchain4j.store.embedding.EmbeddingStore;

// @Service
// public class AiService {

//     private final OpenAiChatModel model;

//     @Autowired
//     private EmbeddingModel embeddingModel;

//     @Autowired
//     private EmbeddingStore<TextSegment> embeddingStore;

//     public AiService(@Value("${openai.api.key}") String apiKey) {
//         this.model = OpenAiChatModel.builder()
//                 .apiKey(apiKey)
//                 .build();
//     }

//     public String ask(String question) {
//         System.out.println("Received question: " + question);
//         // 1. Create embedding for question
//         Embedding queryEmbedding = embeddingModel.embed(question).content();

//         //2. Retrieve matching TextSegments
//         int TOP_K = 10;
//         List<EmbeddingMatch<TextSegment>> matches =
//                 embeddingStore.findRelevant(queryEmbedding, TOP_K);
//         System.out.println("Matches found = " + matches.size());
//         //3. Extract actual TEXT safely
//         double SCORE_THRESHOLD = 0.75;
//         StringBuilder context = new StringBuilder();
//         int included = 0;
//         for (EmbeddingMatch<TextSegment> match : matches) {
//             if (match.score() >= SCORE_THRESHOLD) {
//             context.append(match.embedded().text()).append("\n");
//             included++;
//             System.out.println("Included chunk (score=" + match.score() + "):");
//             System.out.println("Match chunk: " + match.embedded().text());
//             }
//         }
//         System.out.println("Context chunks used = " + included);
//         //4. Build RAG prompt
//         //old prompt - works well strictly within context
//         //  You are a document-based AI assistant.
//         // You MUST answer ONLY using the context below.
//         // If the answer is not present in the context, reply exactly:
//         // "Information not available in the uploaded document."
//         String prompt = """
//         You are a helpful AI assistant.
//         Use the document context when relevant.
//         If the question is not covered by the document, you may answer using your general knowledge.

//         Context:
//         %s

//         User Question:
//         %s
//         """.formatted(context.toString(), question);

//         //5. Ask LLM
//         return model.generate(prompt);
//     }
// }
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
                .modelName("gpt-4o-mini")
                .temperature(0.3) // Lower temperature for more focused answers
                .build();
    }

    public String ask(String question) {
        System.out.println("Received question: " + question);

        // 1. Create embedding for question
        Embedding queryEmbedding = embeddingModel.embed(question).content();

        // 2. Retrieve matching TextSegments
        int TOP_K = 10;
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, TOP_K);
        System.out.println("Initial matches found = " + matches.size());

        // 3. Extract actual TEXT with score threshold
        double SCORE_THRESHOLD = 0.70;
        StringBuilder context = new StringBuilder();
        int included = 0;

        // Track sections to ensure focused answers
        String dominantSection = null;
        int sectionCount = 0;

        for (EmbeddingMatch<TextSegment> match : matches) {
            if (match.score() >= SCORE_THRESHOLD && included < 5) {
                System.out.println("Match chunk: " + match.embedded().text());
                System.out.println("Match metadata: " + match.embedded().metadata());
                String section = match.embedded().metadata().getString("section");
                System.out.println("Match section: " + section);
                // Track the most common section
                if (included == 0) {
                    dominantSection = section;
                    System.out.println("Setting dominant section to: " + dominantSection);
                    sectionCount = 1;
                } else if (section != null && section.equals(dominantSection)) {
                    sectionCount++;
                }

                if (section != null && !section.isEmpty()) {
                    context.append("[Section: ").append(section).append("]\n");
                }
                context.append(match.embedded().text()).append("\n\n");
                included++;
                System.out.println("Included chunk (score=" + match.score() +
                        ", section=" + section + ")");
            }
        }
        System.out.println("Context chunks used = " + included);
        System.out.println("Dominant section = " + dominantSection);

        // Handle case where no relevant context is found
        // if (included == 0) {
        //     return "I don't have enough relevant information in the uploaded document to answer this question.";
        // }

        // 5. Build strict RAG prompt with section awareness
        String prompt = buildPrompt(question, context.toString(), dominantSection);

        // 6. Ask LLM
        try {
            return model.generate(prompt);
        } catch (Exception e) {
            System.err.println("Error generating response: " + e.getMessage());
            return "Sorry, I encountered an error while processing your question. Please try again.";
        }
    }

    /**
     * Simple detection - just return null to let RAG handle it naturally
     * The section metadata in chunks will guide the LLM automatically
     */
    private String detectTargetSection(String question) {
        // Let the embedding similarity and section metadata do the work
        // No complex detection needed
        return null;
    }

    /**
     * Builds the prompt - simplified to rely on section metadata in context
     */
    private String buildPrompt(String question, String context, String dominantSection) {
        return String.format(
                """
                        You are a precise document assistant.

                        CRITICAL RULES:
                        2. Pay attention to [Section: ...] labels - they show which part of the document the information comes from
                        3. If most context is from one section, focus your answer on that section
                        4. DO NOT mix information from unrelated sections
                        5. If the question is not covered by the document, you may answer using your general knowledge.

                        Context (with section labels):
                        %s

                        Question: %s

                        Answer:
                        """,
                context, question);
    }
}