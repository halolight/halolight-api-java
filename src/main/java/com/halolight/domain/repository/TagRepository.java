package com.halolight.domain.repository;

import com.halolight.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT t FROM Tag t JOIN t.documents dt WHERE dt.document.id = :documentId")
    List<Tag> findByDocumentId(@Param("documentId") String documentId);

    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Tag> findByNameContaining(@Param("search") String search);
}
