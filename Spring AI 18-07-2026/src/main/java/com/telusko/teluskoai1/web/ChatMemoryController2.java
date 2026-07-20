package com.telusko.teluskoai1.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat-memory2")
public class ChatMemoryController2
{
    private final ChatClient chatClient;

    public ChatMemoryController2(ChatClient.Builder builder, JdbcChatMemoryRepository jdbcChatMemoryRepository)
    {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(20)
                .build();

        this.chatClient = builder.defaultSystem(
                        """
                                You are Telusko's friendly AI assistant.
                                Answer clearly, keep responses helpful, and remember what the user tells you.
                                """
                )
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new TutorStyleAdvisor())
                .build();
    }
    @GetMapping("/chat2")
    public String chatWithMemory(@RequestParam String userId, @RequestParam String prompt)
    {
        return chatClient.prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .call()
                .content();

    }
}
