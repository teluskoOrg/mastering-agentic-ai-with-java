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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rag")
public class RagController
{
    @Autowired
    @Qualifier("openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;
    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private CourseSearchTool courseSearchTool;


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
                        .topK(5)
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
    @GetMapping("/get-course-agentic")
    public String getAnswerAgenticRag(@RequestParam String query)
    {
        return chatClient.prompt(query)
                .system(
                        """
                    You are Telusko's AI Course Advisor.

                    If the user asks about:
                    - courses
                    - prices
                    - duration
                    - technologies
                    - what Telusko offers

                    use the searchCourses tool.

                    For greetings or general AI questions,
                    answer directly without using the tool.
                    """
                )
                .tools(courseSearchTool)
                .call()
                .content();
    }

    @GetMapping("/query-rewrite")
    public String queryRewrite(@RequestParam String query)
    {
        String rewrittenQuery=chatClient.prompt()
                .system("Rewrite the user query as a clear, complete search " +
                        "query for a course catalog. Respond ONLY with the " +
                        "rewritten query, nothing else.")
                .user(query)
                .call()
                .content();
        System.out.println("User prompt original: "+query);
        System.out.println("User prompt rewritten: "+rewrittenQuery);
        List<Document> chunks = vectorStore.similaritySearch(
                SearchRequest.builder().query(rewrittenQuery)
                        .topK(6)
                        .build());

              return answerWithContext(query, chunks);
    }
    @GetMapping("/hyde-response")
    public String hydeResponse(@RequestParam String query)
    {
        String  hypothetical=chatClient.prompt()
                .system(
                        """
                    Write a short factural amswer for this course question.
                    Even if you are not completely sure m write a releastic answer.
                    """
                )
                .user(query)
                .call()
                .content();

        System.out.println("User prompt original: "+query);
        System.out.println("Hypothetical answer: "+hypothetical);

        List<Document> chunks = vectorStore.similaritySearch(
                SearchRequest.builder().query(hypothetical)
                        .topK(6)
                        .build());

        return answerWithContext(query, chunks);

    }

    @GetMapping("/filtered-search")
    public String filteredSerach(@RequestParam String query,
                                 @RequestParam(required= false) String level,
                                 @RequestParam(required= false) String category,
                                 @RequestParam(required= false) Integer price)
    {
        System.out.println(level + " " + category + " " + price);

        // empty filter expression
        StringBuilder filter=new StringBuilder();
        //if level is provided, add to filter

        // if category is provided, add to filter
        if(category!= null)
        {
            if(filter.length() > 0) {
                filter.append(" && ");
            }
            filter.append("category == '")
                    .append(santize(category))
                    .append("'");
        }
        if(level!= null)
            if(filter.length() > 0) {
                filter.append(" && ");
            }
        filter.append("level == '")
                .append(santize(level)) // AI & DataSceince ==> AI_and_Data_Science
                .append("'");
        // if maxPrice is provided, add to filter
        if(price!= null)
        {
            if(filter.length() > 0) {
                filter.append(" && ");
            }
            filter.append("price <= ")
                    .append(price);
        }
        System.out.println("Filter expression: " + filter.toString());
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(6);
//        System.out.println();
        System.out.println(filter.length());
        if (filter.length() > 0)
        {
            builder.filterExpression(filter.toString());
        }

        List<Document> chunks = vectorStore.similaritySearch(
                builder.build());
       for(Document doc :chunks)
       {
           System.out.println("response from vectore store");
           System.out.println(doc.getText());
       }

        return answerWithContext(query, chunks);
    }

    private String santize(String value)
    {
        if (value == null || value.isEmpty()) return "Unknown";
        return value
                .replaceAll("&", "and")
                .replaceAll("[\\s,/]+", "_")
                .replaceAll("[^a-zA-Z0-9_-]", "")
                .trim();

    }




    @GetMapping("/rerank")
    public String rerank(@RequestParam String query) {
        //
        List<Document> maxChunks = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(10)
                .build());

        if (maxChunks.isEmpty()) {
            return "No Courses found based your request";
        }
        StringBuilder numbered = new StringBuilder();
        for (int i = 0; i < maxChunks.size(); i++) {
            String chunkSnippet = maxChunks.get(i).getText();
            if (chunkSnippet.length() > 300) {
                chunkSnippet.substring(0, 300);
            }
            numbered.append(i + 1)
                    .append(", ")
                    .append(chunkSnippet)
                    .append("\n\n");
        }
        //ask llm to ranking
        // 9, 4, 6
        String llmRankingResponse = chatClient
                .prompt()
                .system(
                        """
                                You rank text chunks by relevance to a user query.
                                Return ONLY the numbers of the best 3 chunks.
                                Example: 3,7,1
                                """
                )
                .user("Query" + query + "Chunks  " + numbered)
                .call()
                .content();
        System.out.println("LLM Ranking response ==> "+ llmRankingResponse);

        List<Document> rerankedDoc = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+")
                .matcher(llmRankingResponse);

        while (matcher.find() && rerankedDoc.size() < 3) {

            int index = Integer.parseInt(matcher.group()) - 1;

            if (index >= 0 && index < maxChunks.size()) {
                rerankedDoc.add(maxChunks.get(index));
            }
            if (rerankedDoc.isEmpty()) {

                rerankedDoc = maxChunks.subList(
                        0,
                        Math.min(3, maxChunks.size())
                );
            }
        }
        System.out.println("Final Re rank chunks");
        for(int i =0; i< rerankedDoc.size(); i++)
        {
            System.out.println("Rank" + (i + 1));
            System.out.println(rerankedDoc.get(i).getText());
        }
            return answerWithContext(query, rerankedDoc);


    }
    private String answerWithContext(String query, List<Document> chunks)
    {

        if(chunks.isEmpty())
        {
            return "No Relevant courses found ";
        }
        String context=chunks.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        return chatClient.prompt()
                .system(
                        """
                    You are Telusko's helpful course advisor.
                    Use only the provided course catalog context.
                    Mention course name, price, duration, and level.
                    If the answer is not in the context, politely say so.
                    """
                )
                .user("Context:\n" + context + "\n\nUser Query:\n" + query)
                .call()
                .content();

    }


}

