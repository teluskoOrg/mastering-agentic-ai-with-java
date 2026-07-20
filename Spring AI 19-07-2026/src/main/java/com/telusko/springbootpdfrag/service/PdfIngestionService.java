package com.telusko.springbootpdfrag.service;

import com.telusko.springbootpdfrag.exceptions.DocumentNotFoundException;
import com.telusko.springbootpdfrag.exceptions.EmptyPdfException;
import com.telusko.springbootpdfrag.model.IngestedDocument;
import com.telusko.springbootpdfrag.repo.IngestedDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

@Service
public class PdfIngestionService
{
    private static final Logger log= LoggerFactory.getLogger(PdfIngestionService.class);

    public static final String META_USER_ID = "userID";

    public static final String META_DOCUMENT_HASH= "documentHash";

    public static final String META_FILENAME= "filename";

      private IngestedDocumentRepository ledger;

      private VectorStore vectorStore;

      private JdbcTemplate jdbcTemplate;

    private IngestionProperties properties;


    public PdfIngestionService(IngestedDocumentRepository ledger,
                               VectorStore vectorStore,
                               JdbcTemplate jdbcTemplate,
                               IngestionProperties properties )
    {
    this.ledger=ledger;
    this.vectorStore=vectorStore;
    this.jdbcTemplate=jdbcTemplate;
    this.properties=properties;
    }


    public IngestionResult ingest(Resource pdf,
                                  String filename,
                                  String userId)
    {
            FileFingerprint fingerprint= fingerprint(pdf);
            log.info("in porcess", filename, userId, fingerprint.shortHash());
        Optional<IngestedDocument> alreadyIngested = ledger.findByUserIdAndFileHash(userId, fingerprint.shortHash());


        if(alreadyIngested.isPresent())
        {
            log.info("Skipping as document is ingested", filename, alreadyIngested.get().getId(),
                    alreadyIngested.get().getIngestedAt());
            System.out.println("Skipped ingestion");
            return IngestionResult.skippedDuplicateIngest(alreadyIngested.get());
        }

        List<Document> pages = readPages(pdf);
        if(pages.isEmpty())
        {
            throw new EmptyPdfException(filename);
        }

       List<Document> chunks= split(pages, fingerprint.hash(), filename, userId);
        if(chunks.isEmpty())
        {
            throw new EmptyPdfException(filename);
        }
        vectorStore.add(chunks);
        System.out.println("Ingested");
        return record(fingerprint, filename, userId, pages.size(), chunks);

    }

    private IngestionResult record(FileFingerprint fingerprint, String filename, String userId, int pageCount, List<Document> chunks)
    {
        IngestedDocument document = new IngestedDocument(fingerprint.hash,
                filename,
                userId,
                fingerprint.sizeBytes(),
                pageCount,
                chunks.size());
        try
        {
            IngestedDocument saved = ledger.saveAndFlush(document);
            log.info("Ingested as document ", filename, saved.getId(), saved.getChunkCount());
            return IngestionResult.ingest(saved);
        }
        catch(DataIntegrityViolationException raceLost)
        {
                log.warn("Concurrent ingestion won the race", filename
                );
                deleteChunks(chunks);
             return ledger.findByUserIdAndFileHash(userId, fingerprint.hash())
                     .map(IngestionResult::skippedDuplicateIngest)
                     .orElseThrow(()->raceLost );

        }
    }
    private void deleteChunks(List<Document> chunks) {

        vectorStore.delete(
                chunks.stream()
                        .map(Document::getId)
                        .toList());
    }

    private List<Document> split(List<Document> pages, String fileHash, String filename, String userId)
    {
        TokenTextSplitter splitter = TokenTextSplitter
                .builder()
                .withChunkSize(properties.chunkSize())
                .build();
        return splitter.apply(pages).stream()
                .filter(chunk -> StringUtils.hasText(chunk.getText()))
                .map(chunk ->
                {
                   Map<String, Object> metadata= new HashMap<>(chunk.getMetadata());
                   metadata.put(META_USER_ID, userId);
                   metadata.put(META_DOCUMENT_HASH, fileHash);
                   metadata.put(META_FILENAME, filename);

                   return new Document(chunk.getText(), metadata);
                })
                .toList();

    }

    private FileFingerprint fingerprint(Resource pdf)
    {
        try(InputStream inputStream = pdf.getInputStream())
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // read the pdf 8kb at a time or in any small blocks
            byte[] buffer = new byte[8192];

            long size=0;

            int read;

            // read till end of the file
            while((read = inputStream.read(buffer)) !=-1)
            {
                digest.update(buffer, 0, read);

                size +=read;
            }
            return new FileFingerprint(HexFormat.of().formatHex(digest.digest()), size);


        }
        catch(Exception e)
        {
            throw new RuntimeException("Some issue ", e);

        }
    }
    private List<Document> readPages(Resource pdf)
    {
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPagesPerDocument(properties.pagesPerDocument())
                .build();

        return new PagePdfDocumentReader(pdf, config).get();
    }

    public List<IngestedDocument> listDocument(String userId)
    {
        return ledger.findByuserIdOrderByIngestedAtDesc(userId);
    }

    public IngestedDocument requiredDocument(Long documentId, String userId)
    {
        return ledger.findById(documentId)
                .filter(doc -> doc.getId().equals(userId))

                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }
    /**
     * Deletes a document and all
     * its embeddings.
     */
    @Transactional
    public void deleteDocument(Long documentId,
                               String userId) {


        IngestedDocument document =
                requiredDocument(documentId, userId);

        // Delete all embeddings
        // from the Vector Database.
        int deletedChunks =
                jdbcTemplate.update(

                        "DELETE FROM vector_store "
                                + "WHERE metadata ->> 'documentHash' = ? "
                                + "AND metadata ->> 'userId' = ?",

                        document.getFileHash(),
                        userId);

        // Delete document metadata
        // from relational database.
        ledger.delete(document);

        // Print log message.
        log.info("Deleted document {} ('{}') and {} chunks",
                documentId,
                document.getFilename(),
                deletedChunks);
    }

    private record FileFingerprint(String hash, long sizeBytes)
    {
        String shortHash()
        {
            return hash.substring(0,12);
        }
    }

}
