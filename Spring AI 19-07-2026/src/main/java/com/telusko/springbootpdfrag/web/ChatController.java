package com.telusko.springbootpdfrag.web;

import com.telusko.springbootpdfrag.service.PdfIngestionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdf")
public class ChatController
{
    private  ChatClient chatClent;
    private ChatClient chatClient;
    private VectorStore vectorStore;
    private PdfIngestionService ingestionService;


    public ChatController(OpenAiChatModel chatModel, VectorStore vectorStore, PdfIngestionService ingestionService)
    {
        this.chatClient= ChatClient.create(chatModel);
        this.vectorStore=vectorStore;
        this.ingestionService=ingestionService;
    }
    @GetMapping("/chat")
    public String ask(@RequestParam String query
            , @RequestParam String userId,
                      @RequestParam(required = false) Long documentId)
    {
        return chatClient.prompt(query)
                .system(
                        """
            You are answering questions about documents the user has uploaded.
            Answer using ONLY the retrieved document context.
            If the answer is not in the context, say
            "I couldn't find that in your uploaded documents."
            Cite the source filename when possible.
            """
                ).advisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(8)
                                        .similarityThreshold(0.25)
                                        .filterExpression(scopeFilter(userId, documentId))
                                        .build())
                                .build()).call().content();
    }
    private Filter.Expression scopeFilter(String userId,
                                          Long documentId) {

        // Create a Filter Builder.
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        // --------------------------------------------------
        // Case 1:
        // No document selected.
        //
        // Search ALL documents that belong
        // to the current user.
        // --------------------------------------------------
        if (documentId == null) {

            return b.eq(
                            PdfIngestionService.META_USER_ID,
                            userId)
                    .build();
        }

        // --------------------------------------------------
        // Case 2:
        // User selected one specific document.
        //
        // First verify that the document
        // belongs to this user.
        // --------------------------------------------------
        String documentHash =
                ingestionService
                        .requiredDocument(documentId, userId)
                        .getFileHash();

        // --------------------------------------------------
        // Build a filter containing BOTH:
        //
        // userId
        // AND
        // documentHash
        //
        // So Vector Search searches only
        // this user's selected document.
        // --------------------------------------------------
        return b.and(

                b.eq(
                        PdfIngestionService.META_USER_ID,
                        userId),

                b.eq(
                        PdfIngestionService.META_DOCUMENT_HASH,
                        documentHash)

        ).build();
    }
}
