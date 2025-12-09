package com.example.demo.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;

// @Service
// public class DocumentService {

//     @Autowired
//     private EmbeddingModel embeddingModel;
//     @Autowired
//     private EmbeddingStore<TextSegment> embeddingStore;
   
//     @SuppressWarnings("unchecked")
//     public String processDocument(MultipartFile file){
//         try {
//             String text = new String(file.getBytes(),StandardCharsets.UTF_8);
//             List<String> chunks = chunkText(text, 300);
//             // 1. Convert String → TextSegment
//             for (String chunk : chunks) {
//                 TextSegment segment = TextSegment.from(chunk);
//                 Embedding embedding = embeddingModel.embed(segment).content();
//                 embeddingStore.add(embedding, segment);
//               }
//               return "Document processed and embeddings stored successfully.";    
//         } catch(Exception e){
//              e.printStackTrace();
//             return "Error processing document: " + e.getMessage();
//         }
//     }

//     private List<String> chunkText(String text, int chunkSize) {
//         List<String> chunks =  new ArrayList<>();
//         String[] words = text.split("\\s+");
//         StringBuilder currentChunk = new StringBuilder();
//         for(String word : words){
//             currentChunk.append(word).append(" ");
//             if(currentChunk.length() >= chunkSize){
//                 chunks.add(currentChunk.toString().trim());
//                 currentChunk = new StringBuilder();
//             }
//         }
//         if(!currentChunk.isEmpty()){
//             chunks.add(currentChunk.toString().trim());
//         }
//         return chunks;
//     }

//     // private String extractText(MultipartFile file) throws IOException {
//     //     String filename = file.getOriginalFilename();
//     //     if(filename != null){
//     //         if(filename.endsWith(".pdf")){
//     //         return extractPdfText(file);
//     //     } else if (filename.endsWith(".txt")) {
//     //         return new String(file.getBytes());
//     //      }
//     //     }        
//     //     throw new IllegalArgumentException("Unsupported file type");
//     // }

//     // private String extractPdfText(MultipartFile file) throws IOException {
//     //     PDDocument pdDocument = PDDocument.load(file.getInputStream());
//     //     PDFTextStripper stripper = new PDFTextStripper();
//     //     String text = stripper.getText(pdDocument);
//     //     pdDocument.close();
//     //     return text;
//     // }

// }

@Service
public class DocumentService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    public String processDocument(MultipartFile file) {
        try {
            String text = extractText(file);          // ✅ use proper extraction

            List<String> chunks = chunkText(text, 300);
            System.out.println("Chunk count = " + chunks.size());

            for (String chunk : chunks) {
                TextSegment segment = TextSegment.from(chunk);
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
            }

            return "Document processed and embeddings stored successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing document: " + e.getMessage();
        }
    }

    private String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename != null) {
            if (filename.endsWith(".pdf")) {
                return extractPdfText(file);          // PDFBox
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
            System.out.println("Extracted PDF text (first 300 chars): "
                    + text.substring(0, Math.min(300, text.length())));
            return text;
        }
    }

    // chunkText stays as you already wrote it
    private List<String> chunkText(String text, int chunkSize) {
        List<String> chunks =  new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentChunk = new StringBuilder();
        for(String word : words){
            currentChunk.append(word).append(" ");
            if(currentChunk.length() >= chunkSize){
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }
        }
        if(!currentChunk.isEmpty()){
            chunks.add(currentChunk.toString().trim());
        }
        return chunks;
    }
}


