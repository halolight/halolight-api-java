package com.halolight.domain.repository;

import com.halolight.domain.entity.StorageFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageFileRepository extends JpaRepository<StorageFile, String> {

    Page<StorageFile> findByOwnerId(String ownerId, Pageable pageable);

    List<StorageFile> findByFolderId(String folderId);

    Page<StorageFile> findByTeamId(String teamId, Pageable pageable);

    @Query("SELECT f FROM StorageFile f WHERE " +
            "f.ownerId = :ownerId AND " +
            "(:folderId IS NULL OR f.folderId = :folderId) AND " +
            "(:type IS NULL OR f.type = :type) AND " +
            "(:search IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<StorageFile> findByOwnerIdAndFilters(
            @Param("ownerId") String ownerId,
            @Param("folderId") String folderId,
            @Param("type") String type,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT SUM(f.size) FROM StorageFile f WHERE f.ownerId = :ownerId")
    Long sumSizeByOwnerId(@Param("ownerId") String ownerId);

    long countByFolderId(String folderId);
}
