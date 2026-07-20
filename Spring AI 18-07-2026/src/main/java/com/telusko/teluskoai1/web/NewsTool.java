package com.telusko.teluskoai1.web;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NewsTool
{
     RestTemplate restTemplate= new RestTemplate();
     @Tool(description = "Get latest news and current headlines")
     public String getLatestNews(String topic)
     {
         System.out.println("News Tool");
         String apiKey= "7e1bc";
         String url= "https://newsapi.org/v2/everything?q="+ topic +"&apiKey="+apiKey;
         String result=restTemplate.getForObject(url, String.class);
         return result;
     }
}
