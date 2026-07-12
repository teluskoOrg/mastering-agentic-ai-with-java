package com.telusko.teluskoai1.web;


import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;

public class TutorStyleAdvisor implements CallAdvisor
{

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain)
    {
        // 1. Get the original user prompt from the request
        String originalPrompt = chatClientRequest.prompt().getContents();
        // 2. Modify the prompt to include a tutor-style instruction
        String modifiedPrompt = originalPrompt + "\n\nPlease answer in a tutor-style manner, providing clear explanations and examples where appropriate.";
        Prompt newPrompt = new Prompt(modifiedPrompt);
        // 3. Create a new ChatClientRequest with the modified prompt
        ChatClientRequest newChatClientRequest=chatClientRequest.mutate()
                .prompt(newPrompt)
                .build();
        return callAdvisorChain.nextCall(newChatClientRequest);
    }

    @Override
    public String getName() {
        return "TutorStyleAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
