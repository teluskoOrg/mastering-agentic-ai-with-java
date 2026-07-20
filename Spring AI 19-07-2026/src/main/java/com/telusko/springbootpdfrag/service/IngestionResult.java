package com.telusko.springbootpdfrag.service;

import com.telusko.springbootpdfrag.model.IngestedDocument;


public record IngestionResult
        (
                Status status,
         IngestedDocument document
        )
{
    public enum Status {
        INGESTED,// new pdf processed
        SKIPPED_DUPLICATE // same pdf processed earlier and no new embeddings hence skipping

    }
    public static IngestionResult ingest(IngestedDocument ingestedDocument)
    {
        return new IngestionResult(Status.INGESTED, ingestedDocument);
    }

    public static IngestionResult skippedDuplicateIngest(IngestedDocument existingDoc)
    {
        return new IngestionResult(Status.SKIPPED_DUPLICATE, existingDoc);
    }

    public boolean wasSkipped()
    {
        return status == Status.SKIPPED_DUPLICATE;
    }
}
