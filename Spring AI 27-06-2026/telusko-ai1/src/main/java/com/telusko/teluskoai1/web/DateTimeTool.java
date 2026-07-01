package com.telusko.teluskoai1.web;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class DateTimeTool
{
    @Tool(description = "Get Current date and time for user's time zone")
    public String getCurrentDateTime(String timeZone)
    {
        System.out.println("Default time zone");
//        return java.time.LocalDateTime.now().toString();
        return ZonedDateTime.now(ZoneId.of(timeZone)).toString();
    }
}
