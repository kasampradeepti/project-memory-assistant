// package com.example.demo.service;

// import java.io.IOException;
// import java.nio.charset.StandardCharsets;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;

// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.apache.pdfbox.text.PDFTextStripper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import dev.langchain4j.model.embedding.EmbeddingModel;
// import dev.langchain4j.store.embedding.EmbeddingStore;
// import dev.langchain4j.data.document.Metadata;
// import dev.langchain4j.data.embedding.Embedding;
// import dev.langchain4j.data.segment.TextSegment;

// @Service
// public class DocumentService {

//     @Autowired
//     private EmbeddingModel embeddingModel;

//     @Autowired
//     private EmbeddingStore<TextSegment> embeddingStore;

//     // RAW TEXT
//     // → detect headings
//     // → build sections
//     // → chunk inside each section
//     // → embeddings + metadata(section)

//     public String processDocument(MultipartFile file) {
//     try {
//         String text = extractText(file);

//         List<Section> sections = splitIntoSections(text);
//         System.out.println("Detected sections = " + sections.size());

//         for (Section section : sections) {

//             List<String> chunks = chunkText(section.content, 500);

//             for (String chunk : chunks) {
//                 Metadata metadata = Metadata.from(
//         Map.of(
//             "section", section.name,
//             "source", file.getOriginalFilename()
//              )
// );
//                 TextSegment segment = TextSegment.from(chunk, metadata);

//                 Embedding embedding =
//                         embeddingModel.embed(segment.text()).content();

//                 embeddingStore.add(embedding, segment);
//             }
//         }

//         return "Document processed with section-aware chunking.";
//     } catch (Exception e) {
//         e.printStackTrace();
//         return "Error processing document: " + e.getMessage();
//         }
//     }

//     private String extractText(MultipartFile file) throws IOException {
//         String filename = file.getOriginalFilename();
//         if (filename != null) {
//             if (filename.endsWith(".pdf")) {
//                 return extractPdfText(file); // PDFBox
//             } else if (filename.endsWith(".txt") || filename.endsWith(".md")) {
//                 return new String(file.getBytes(), StandardCharsets.UTF_8);
//             }
//         }
//         throw new IllegalArgumentException("Unsupported file type");
//     }

//     private String extractPdfText(MultipartFile file) throws IOException {
//         try (PDDocument pdDocument = PDDocument.load(file.getInputStream())) {
//             PDFTextStripper stripper = new PDFTextStripper();
//             String text = stripper.getText(pdDocument);
//             System.out.println("PDF text length: " + text.length());
//             System.out.println("Extracted PDF text (first 300 chars): "
//                     + text.substring(0, Math.min(300, text.length())));
//             return text;
//         }
//     }

//     private boolean isHeading(String line) {
//     line = line.trim();
//     return
//         line.matches("^\\d+(\\.\\d+)*\\s+.+") || // 1. / 2.1
//         line.startsWith("#") ||                  // markdown
//         line.endsWith(":") ||                    // Backend:
//         (line.length() < 80 && line.matches("^[A-Z][A-Za-z\\s\\(\\)-]+$"));
//     }

//     private String normalizeHeading(String heading) {
//     return heading .replaceAll("^#+", "")
//             .replaceAll("^\\d+(\\.\\d+)*", "")
//             .replace(":", "")
//             .trim();
//     }

//     private List<Section> splitIntoSections(String text) {

//     List<Section> sections = new ArrayList<>();

//     String currentSection = "General";
//     StringBuilder buffer = new StringBuilder();

//     for (String line : text.split("\\R")) {
//         if (isHeading(line)) {
//             if (!buffer.isEmpty()) {
//                 sections.add(new Section(currentSection, buffer.toString()));
//             }
//             currentSection = normalizeHeading(line);
//             buffer = new StringBuilder();
//         } else {
//             buffer.append(line).append("\n");
//         }
//     }

//     if (!buffer.isEmpty()) {
//         sections.add(new Section(currentSection, buffer.toString()));
//     }

//     return sections;
//     }

//     static class Section {
//     String name;
//     String content;

//     Section(String name, String content) {
//         this.name = name;
//         this.content = content;
//     }
// }

//     // chunkText stays as you already wrote it
//     private List<String> chunkText(String text, int chunkSize) {
//         List<String> chunks = new ArrayList<>();
//         String[] words = text.split("\\s+");
//         StringBuilder currentChunk = new StringBuilder();
//         for (String word : words) {
//             currentChunk.append(word).append(" ");
//             if (currentChunk.length() >= chunkSize) {
//                 chunks.add(currentChunk.toString().trim());
//                 currentChunk = new StringBuilder();
//             }
//         }
//         if (!currentChunk.isEmpty()) {
//             chunks.add(currentChunk.toString().trim());
//         }
//         return chunks;
//     }

// }

package com.example.demo.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;

@Service
public class DocumentService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * Clear all stored embeddings (useful for testing)
     * Call this before re-uploading documents to start fresh
     */
    public String clearAllDocuments() {
        try {
            // Note: ChromaEmbeddingStore doesn't have a clear method
            // You need to restart ChromaDB or delete the collection manually
            // Or you can use ChromaDB's REST API to delete the collection
            return "To clear documents, please restart ChromaDB or delete the 'project_docs' collection manually.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String processDocument(MultipartFile file) {
        try {
            String text = extractText(file);

            List<Section> sections = splitIntoSections(text);
            System.out.println("\n=== DETECTED SECTIONS ===");
            System.out.println("Total sections = " + sections.size());
            for (int i = 0; i < sections.size(); i++) {
                System.out.println(
                        (i + 1) + ". '" + sections.get(i).name + "' (" + sections.get(i).content.length() + " chars)");
            }
            System.out.println("========================\n");

            for (Section section : sections) {
                System.out.println("Processing section: " + section.name);

                List<String> chunks = chunkText(section.content, 500);
                System.out.println("  Chunks in this section: " + chunks.size());

                for (int i = 0; i < chunks.size(); i++) {
                    String chunk = chunks.get(i);

                    // Create metadata map properly
                    Map<String, String> metadataMap = new HashMap<>();
                    metadataMap.put("section", section.name);
                    metadataMap.put("source", file.getOriginalFilename());

                    // Create Metadata object from map
                    Metadata metadata = new Metadata(metadataMap);

                    // Create TextSegment with metadata
                    TextSegment segment = TextSegment.from(chunk, metadata);

                    // Verify metadata before storing
                    System.out.println("  ✓ Chunk " + i + ":");
                    System.out.println("    Section: " + segment.metadata().getString("section"));
                    System.out.println("    Source: " + segment.metadata().getString("source"));
                    System.out.println("    Text preview: " + chunk.substring(0, Math.min(50, chunk.length())));

                    // Generate embedding
                    Embedding embedding = embeddingModel.embed(segment.text()).content();

                    // Store embedding with segment in ChromaDB
                    embeddingStore.add(embedding, segment);
                    System.out.println("    Stored in ChromaDB ✓");
                }
            }

            return "Document processed with section-aware chunking. Total sections: " + sections.size();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing document: " + e.getMessage();
        }
    }

    private String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename != null) {
            if (filename.endsWith(".pdf")) {
                return extractPdfText(file);
            } else if (filename.endsWith(".txt") || filename.endsWith(".md")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        }
        throw new IllegalArgumentException("Unsupported file type");
    }

    private String extractPdfText(MultipartFile file) throws IOException {
        try (PDDocument pdDocument = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdDocument);
            System.out.println("PDF text length: " + text.length());
            return text;
        }
    }

    private boolean isHeading(String line) {
        line = line.trim();
        if (line.isEmpty())
            return false;

        return line.matches("^\\d+(\\.\\d+)*\\.?\\s+.+") || // 1. / 2.1 / 3.1. / 4. Architecture
                line.startsWith("#") || // markdown # headers
                (line.endsWith(":") && line.length() < 80) || // Backend: (short lines ending with :)
                (line.length() < 80 && line.matches("^[A-Z][A-Za-z\\s\\(\\)\\-]+$")); // Title Case Lines
    }

    private String normalizeHeading(String heading) {
        return heading
                .replaceAll("^#+\\s*", "") // Remove markdown # and spaces
                .replaceAll("^\\d+(\\.\\d+)*\\.?\\s*", "") // Remove numbering like 1. or 2.1. or 4.
                .replaceAll(":$", "") // Remove trailing colon
                .trim();
    }

    private List<Section> splitIntoSections(String text) {
        List<Section> sections = new ArrayList<>();
        String currentSection = "General";
        StringBuilder buffer = new StringBuilder();

        String[] lines = text.split("\\R");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (isHeading(line)) {
                // Save previous section if it has content
                String content = buffer.toString().trim();
                if (!content.isEmpty()) {
                    sections.add(new Section(currentSection, content));
                    System.out
                            .println("  Section created: '" + currentSection + "' with " + content.length() + " chars");
                }

                // Start new section
                currentSection = normalizeHeading(line);
                buffer = new StringBuilder();
            } else {
                // Only add non-empty lines to avoid extra whitespace
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    buffer.append(line).append("\n");
                }
            }
        }

        // Add last section
        String content = buffer.toString().trim();
        if (!content.isEmpty()) {
            sections.add(new Section(currentSection, content));
            System.out.println("  Section created: '" + currentSection + "' with " + content.length() + " chars");
        }

        return sections;
    }

    static class Section {
        String name;
        String content;

        Section(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }

    private List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentChunk = new StringBuilder();

        for (String word : words) {
            currentChunk.append(word).append(" ");
            if (currentChunk.length() >= chunkSize) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}
