package com.telusko.springbootpdfrag.repo;

import com.telusko.springbootpdfrag.model.IngestedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface IngestedDocumentRepository extends JpaRepository<IngestedDocument, Long>
{
    Optional<IngestedDocument> findByUserIdAndFileHash(String userId, String fileHash);
    List<IngestedDocument> findByuserIdOrderByIngestedAtDesc(String userId);
}
