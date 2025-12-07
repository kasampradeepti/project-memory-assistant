package com.example.demo.service;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {
   
    public String processDocument(MultipartFile file){
        try {
            String text = extractText(file);
            System.out.println("Extracted text:\n" + text);
            return "Document processed successfully";

        } catch(Exception e){
             e.printStackTrace();
            return "Error processing document: " + e.getMessage();
        }

    }

    private String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if(filename != null){
            if(filename.endsWith(".pdf")){
            return extractPdfText(file);
        } else if (filename.endsWith(".txt")) {
            return new String(file.getBytes());
         }
        }        
        throw new IllegalArgumentException("Unsupported file type");
    }

    private String extractPdfText(MultipartFile file) throws IOException {
        PDDocument pdDocument = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(pdDocument);
        pdDocument.close();
        return text;
    }

}

