package com.telusko.teluskoai1.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class RagController
{
    @Autowired
    @Qualifier("openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;
    @Autowired
    private VectorStore vectorStore;

    private ChatClient chatClient;
    public RagController(OpenAiChatModel chatModel)
    {
        this.chatClient = ChatClient.create(chatModel);
    }
    @PostMapping("/embeddings")
    public float[] embeddings(@RequestParam String text)
    {
        return embeddingModel.embed(text);
    }
    @GetMapping("/get-course")
    public String getAnswerWithRag(@RequestParam String query)
    {
        return chatClient
                .prompt(query)
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .call()
                .content();

        // --> Spring boot app / get-course --> advisors --> pg vector table
        // --> cosine similarity search--> top 4 chunks
        // --> prompt template --> chat model --> answer
    }
    @GetMapping("/search-without-llm")
    public List<Document> searchWithoutLLM(@RequestParam String query,
                                           @RequestParam(defaultValue = "4") int topK)
    {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(0.7)
                        //depends on embedding model and vector dtaabase
                        //and also data quality for ex: 0.5 loose match
                        //0.8 good match
                        .build());

    }
    @GetMapping("/get-course-controlled")
    public String getAnswerWithRagLLM(@RequestParam String query)
    {
        return chatClient
                .prompt(query)
                .system("""
                        You are Telusko's course advisor.
                        Answer using only the context provided from the course catalog.
                        If the answer is not in the context, say
                        "I don't have that course information right now."
                        Always mention course name, price, duration, and level when relevant.
                        """)

                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .topK(7)
                                .similarityThreshold(0.7)
                                .build())
                        .build())
                .call()
                .content();


    }
}
