package com.telusko.springbootpdfrag.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ingestion")
public record IngestionProperties(
        String inboxFolder,
        String defaultUserId,
        int chunkSize,
        int pagesPerDocument


) {
}
