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

import java.util.List;

@Component
public class DataInitializer
{
    @Autowired
    private  VectorStore vectorStore;
    @PostConstruct
    public void initData()
    {
        List<Document> existingDocs= vectorStore.similaritySearch(
                SearchRequest.builder().query("Telusko").topK(1).build());
        if (existingDocs != null && !existingDocs.isEmpty())
        {
            System.out.println("Data already exists in the vector store. Skipping initialization.");
            return;
        }

        System.out.println("Data does not exist in the vector store. Initializing data...");
        TextReader textReader = new TextReader(new ClassPathResource("Courses.txt"));
        TokenTextSplitter splitter = TokenTextSplitter
                .builder()
                .withChunkSize(500)
                .build();
        List<Document> docs = splitter.split(textReader.get());
        vectorStore.add(docs);



//        TextReader textReader = new TextReader(new ClassPathResource("Courses.txt"));
//        TokenTextSplitter splitter = TokenTextSplitter
//                .builder()
//                .withChunkSize(500)
//                .build();
//        List<Document> docs = splitter.split(textReader.get());
//        vectorStore.add(docs);
    }


}
