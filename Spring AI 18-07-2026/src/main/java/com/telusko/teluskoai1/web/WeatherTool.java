package com.telusko.teluskoai1.web;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherTool
{
    private final RestTemplate restTemplate= new RestTemplate();
    @Tool(description="Get current weather information for a given city")
    public String getCurrentWeather
            (@ToolParam(description = "City name, for example Mumbai, Pune, Bangalore, London") String location)

    {
        System.out.println("Weather Tool called for: " + location);
        try
        {
            String url = "https://wttr.in/" + location + "?format=3";
            return restTemplate.getForObject(url, String.class);
        }
        catch(Exception e)
        {
            return "Error fetching weather information for " + location + ": " + e.getMessage();
        }

    }
}
