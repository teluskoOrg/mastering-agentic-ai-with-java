package com.telusko;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.stream.Collectors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();
        System.out.println("Client created successfully.");
        ResponseCreateParams params = ResponseCreateParams.builder()
                .input("Recommend a good book to read.")
                .model("gpt-5.5")
                .build();

        Response response = client.responses().create(params);
        System.out.println("Response created successfully.");
        System.out.println(response);
//        String output= response.output()
//                        .stream()
//                                .flatMap(item -> item.message().stream())
//                                        .flatMap(message -> message.content().stream())
//                                                .flatMap(content -> content.outputText().stream())
//                                                        .map(text -> text.text())
//                                                                .collect(Collectors.joining());

//        System.out.println(output);
    }
}