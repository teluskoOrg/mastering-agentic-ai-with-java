package com.telusko.teluskoai1.web;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseSearchTool
{
    @Autowired
    private VectorStore vectorStore;

    @Tool(description = "Search Telusko's course catalog for courses matching a topic, " +
            "technology, or skill. Use this whenever the user asks about " +
            "courses, prices, durations, or what Telusko offers.")
    public String searchCourses(@ToolParam(description = "The topic to " +
            "search for like Java, AI, DevOps") String topic)
    {
        System.out.println("Course tool is searching for "+ topic);
        List<Document> result = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(topic)
                        .topK(6)
                        .build());
        return result.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
        //extract text from the document and return it as a one string

    }
}
