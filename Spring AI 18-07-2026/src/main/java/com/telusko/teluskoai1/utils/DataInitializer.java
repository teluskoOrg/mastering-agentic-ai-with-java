package com.telusko.teluskoai1.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer
{
    @Autowired
    private  VectorStore vectorStore;

    //
    @PostConstruct
    public void initData() {
        List<Document> existingDocs = vectorStore.similaritySearch(
                SearchRequest.builder().query("Telusko").topK(1).build());
        if (existingDocs != null && !existingDocs.isEmpty()) {
            System.out.println("Data already exists. Skipping initialization.");
            return;
        }

        // Read all courses from text file
        System.out.println("Initializing vector store with courses...");
        List<Document> docs = parseCourses();
        vectorStore.add(docs);
        System.out.println("Added " + docs.size() + " course documents.");
    }

    private List<Document> parseCourses() {

        // Store all courses doc
        List<Document> docs = new ArrayList<>();

        //read from courses.text from res folder
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("Courses.txt").getInputStream(),
                        StandardCharsets.UTF_8))) {


            // Stores one complelete course temporarily
            StringBuilder block = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    // Blank line marks end of a course block
                    if (block.length() > 0) {
                        docs.add(buildDocument(block.toString()));
                        block.setLength(0);// clear buffer for the next course
                    }
                } else {
                    block.append(line).append("\n");
                }
            }
            // Last block if file doesn't end with blank line
            if (block.length() > 0) {
                docs.add(buildDocument(block.toString()));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Courses.txt", e);
        }

        return docs;
    }

    private Document buildDocument(String block) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "Courses.txt");

        // Extract category, level, price from block
        String category = extractField(block, "Category:");
        String level = extractField(block, "Level:");
        String price = extractField(block, "Price:");

        metadata.put("category", sanitize(category));
        metadata.put("level", sanitize(level));

        // Parse price as integer (remove $ and text)
        // $299 --> 299
        if (price != null) {
            String priceNumber = price.replaceAll("[^0-9]", "");
            if (!priceNumber.isEmpty()) {
                metadata.put("price", Integer.parseInt(priceNumber));
            }
        }

        System.out.println("Loaded course with metadata: " + metadata);
        return new Document(block.trim(), metadata);
    }

    private String extractField(String block, String label) {
        for (String line : block.split("\n")) {
            if (line.trim().startsWith(label)) {
                return line.substring(line.indexOf(label) + label.length()).trim();
            }
        }
        return null;
    }

    private String sanitize(String value) {
        if (value == null || value.isEmpty()) return "Unknown";
        return value
                .replaceAll("&", "and")
                .replaceAll("[\\s,/]+", "_")
                .replaceAll("[^a-zA-Z0-9_-]", "")
                .trim();
    }
//    @PostConstruct
//    public void initData()
//    {
//        List<Document> existingDocs= vectorStore.similaritySearch(
//                SearchRequest.builder().query("Telusko").topK(1).build());
//        if (existingDocs != null && !existingDocs.isEmpty())
//        {
//            System.out.println("Data already exists in the vector store. Skipping initialization.");
//            return;
//        }
//
//        System.out.println("Data does not exist in the vector store. Initializing data...");
//       // TextReader textReader = new TextReader(new ClassPathResource("Courses.txt"));
//        TextReader textReader = new TextReader(
//                new ClassPathResource("Courses.txt")
//        );
//
//
//        TokenTextSplitter splitter = TokenTextSplitter
//                .builder()
//                .withChunkSize(500)
//                .build();
//        List<Document> docs = splitter.split(textReader.get());
//
//
//        vectorStore.add(docs);



//        TextReader textReader = new TextReader(new ClassPathResource("Courses.txt"));
//        TokenTextSplitter splitter = TokenTextSplitter
//                .builder()
//                .withChunkSize(500)
//                .build();
//        List<Document> docs = splitter.split(textReader.get());
//        vectorStore.add(docs);



}
