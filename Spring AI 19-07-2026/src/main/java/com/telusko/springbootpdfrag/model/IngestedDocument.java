package com.telusko.springbootpdfrag.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingested_documents")
public class IngestedDocument
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 of the raw file bytes, lowercase hex. */
    @Column(name = "file_hash", nullable = false, length = 64, updatable = false)
    private String fileHash;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "user_id", nullable = false, length = 100, updatable = false)
    private String userId;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(name = "page_count", nullable = false)
    private int pageCount;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Column(name = "ingested_at", nullable = false)
    private LocalDateTime ingestedAt;

    /** Required by JPA. */
    protected IngestedDocument() {
    }

    public IngestedDocument(String fileHash,
                            String filename,
                            String userId,
                            long fileSizeBytes,
                            int pageCount,
                            int chunkCount) {
        this.fileHash = fileHash;
        this.filename = filename;
        this.userId = userId;
        this.fileSizeBytes = fileSizeBytes;
        this.pageCount = pageCount;
        this.chunkCount = chunkCount;
        this.ingestedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getFileHash() {
        return fileHash;
    }

    public String getFilename() {
        return filename;
    }

    public String getUserId() {
        return userId;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public LocalDateTime getIngestedAt() {
        return ingestedAt;
    }
}
