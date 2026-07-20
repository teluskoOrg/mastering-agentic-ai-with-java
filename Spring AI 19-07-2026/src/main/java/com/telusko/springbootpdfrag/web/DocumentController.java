package com.telusko.springbootpdfrag.web;

import com.telusko.springbootpdfrag.model.IngestedDocument;
import com.telusko.springbootpdfrag.service.IngestionResult;
import com.telusko.springbootpdfrag.service.PdfIngestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

@RestController
@RequestMapping("/api/docs")
public class DocumentController
{
    private PdfIngestionService service;

   public DocumentController(PdfIngestionService service)
   {
       this.service=service;
   }
   @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   public ResponseEntity<DocumentResponse> upload(@RequestParam("file")MultipartFile file,
                                                  @RequestParam String userId)
   {
       requiredPdf(file);
       IngestionResult result = service.ingest(file.getResource() , file.getOriginalFilename(), userId);
       return result.wasSkipped()? ResponseEntity.ok(DocumentResponse.of(result)):ResponseEntity.status(201)
               .body(DocumentResponse.of(result));
   }
    @GetMapping("/list")
    public List<DocumentResponse> list(@RequestParam String userId) {
        return service.listDocument(userId).stream()
                .map(DocumentResponse::of)
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam String userId) {
        service.deleteDocument(id, userId);
        return ResponseEntity.noContent().build();
    }


    private void requiredPdf(MultipartFile file)
    {
        if(file.isEmpty())
        {
            throw new ResponseStatusException(BAD_REQUEST, "file is empty");
        }
        String filename= file.getOriginalFilename();
        if(filename == null || !filename.toLowerCase().endsWith(".pdf"))
        {
            throw new ResponseStatusException(UNSUPPORTED_MEDIA_TYPE,"Only PDF files are accepted" );
        }
    }

    public record DocumentResponse(Long id, String filename, int pageCount, int chunkCount, long fileSizeBytes, String status)
   {
       static DocumentResponse of(IngestionResult result){
          return of(result.document(), result.status().name());
           }
       static DocumentResponse of(IngestedDocument document){
           return of(document, "INGESTED");
       }
       private static DocumentResponse of(IngestedDocument d, String status)
       {
           return new DocumentResponse(d.getId(), d.getFilename(), d.getPageCount(), d.getChunkCount(), d.getFileSizeBytes(), status);
       }
   }
}
