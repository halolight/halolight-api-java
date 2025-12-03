package com.halolight.domain.repository;

import com.halolight.domain.entity.DocumentShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentShareRepository extends JpaRepository<DocumentShare, String> {

    List<DocumentShare> findByDocumentId(String documentId);

    List<DocumentShare> findBySharedWithId(String sharedWithId);

    List<DocumentShare> findByTeamId(String teamId);

    Optional<DocumentShare> findByDocumentIdAndSharedWithId(String documentId, String sharedWithId);

    Optional<DocumentShare> findByDocumentIdAndTeamId(String documentId, String teamId);

    boolean existsByDocumentIdAndSharedWithId(String documentId, String sharedWithId);

    boolean existsByDocumentIdAndTeamId(String documentId, String teamId);

    void deleteByDocumentId(String documentId);

    List<DocumentShare> findByExpiresAtBefore(Instant now);
}
