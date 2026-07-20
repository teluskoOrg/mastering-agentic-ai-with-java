CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;

-- One row per PDF we have successfully embedded.
-- file_hash is the SHA-256 of the raw file bytes and is what makes
-- ingestion idempotent: re-uploading the same PDF is a no-op.
CREATE TABLE IF NOT EXISTS ingested_documents (
                                                  id              BIGSERIAL PRIMARY KEY,
                                                  file_hash       CHAR(64)     NOT NULL,
    filename        VARCHAR(255) NOT NULL,
    user_id         VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT       NOT NULL,
    page_count      INT          NOT NULL,
    chunk_count     INT          NOT NULL,
    ingested_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Dedup is per user: two tenants uploading the same PDF each get
    -- their own chunks, but neither can ingest it twice. The DB is the
    -- authority here, so a race between two concurrent ingests of the
    -- same file fails loudly instead of double-embedding.
    CONSTRAINT ingested_documents_user_hash_uk UNIQUE (user_id, file_hash)
    );

CREATE INDEX IF NOT EXISTS ingested_documents_user_idx
    ON ingested_documents (user_id);

-- Spring AI's vector store table
CREATE TABLE IF NOT EXISTS vector_store (
                                            id        TEXT PRIMARY KEY,
                                            content   TEXT,
                                            metadata  JSONB,
                                            embedding VECTOR(1536)
    );

CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
    ON vector_store USING HNSW (embedding vector_cosine_ops);

-- Chunk deletion filters on metadata->>'documentHash'; without this index
-- every delete is a full scan of the embeddings table.
CREATE INDEX IF NOT EXISTS vector_store_document_idx
    ON vector_store ((metadata ->> 'documentHash'));
