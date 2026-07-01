package com.telusko.teluskoai1.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class Telusko
{
    private OpenAiChatModel model;
    private ChatClient chatClient;

    @Autowired
    private DateTimeTool dateTimeTool;

    @Autowired
    private NewsTool newsTool;

    ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
    String conversationId= UUID.randomUUID().toString();
//    public Telusko(OpenAiChatModel model)
//    {
//        this.chatClient=ChatClient.create(model);
//        //this.model=model;
//    }
        public Telusko(ChatClient.Builder builder)
        {
            this.chatClient=builder.build();
            this.chatClient=builder
                    .defaultAdvisors(MessageChatMemoryAdvisor
                            .builder(chatMemory)
                            .build()).defaultAdvisors(a ->
                            a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .build();
//            this.chatClient=builder.defaultAdvisors(new MessageCha)
        }
    @GetMapping("/info/{prompt}")
    public String getInfo(@PathVariable String prompt)
    {
       // String response=model.call(prompt);
//        String response=chatClient
//                .prompt(prompt)
//                .call()
//                .content();
        ChatResponse chatResponse = chatClient.prompt(prompt)
//                .tools(dateTimeTool,newsTool)
                .tools(dateTimeTool)
                .call()
                .chatResponse();
        System.out.println(chatResponse.getMetadata().getModel());
        return chatResponse
                .getResult()
                .getOutput()
                .getText();
    }
}
