package com.halolight.domain.repository;

import com.halolight.domain.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    Page<Document> findByOwnerId(String ownerId, Pageable pageable);

    Page<Document> findByTeamId(String teamId, Pageable pageable);

    List<Document> findByFolder(String folder);

    @Query("SELECT d FROM Document d WHERE " +
            "d.ownerId = :ownerId AND " +
            "(:type IS NULL OR d.type = :type) AND " +
            "(:folder IS NULL OR d.folder = :folder) AND " +
            "(:search IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Document> findByOwnerIdAndFilters(
            @Param("ownerId") String ownerId,
            @Param("type") String type,
            @Param("folder") String folder,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT d FROM Document d LEFT JOIN d.shares ds WHERE " +
            "(d.ownerId = :userId OR ds.sharedWithId = :userId OR " +
            "(ds.teamId IN (SELECT tm.id.teamId FROM TeamMember tm WHERE tm.id.userId = :userId)))")
    Page<Document> findAccessibleByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT SUM(d.size) FROM Document d WHERE d.ownerId = :ownerId")
    Long sumSizeByOwnerId(@Param("ownerId") String ownerId);
}
