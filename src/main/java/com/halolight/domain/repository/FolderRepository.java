package com.halolight.domain.repository;

import com.halolight.domain.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, String> {

    List<Folder> findByOwnerId(String ownerId);

    List<Folder> findByParentId(String parentId);

    List<Folder> findByOwnerIdAndParentIdIsNull(String ownerId);

    List<Folder> findByTeamId(String teamId);

    @Query("SELECT f FROM Folder f WHERE " +
            "f.ownerId = :ownerId AND " +
            "(:parentId IS NULL OR f.parentId = :parentId) AND " +
            "(:search IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Folder> findByOwnerIdAndFilters(
            @Param("ownerId") String ownerId,
            @Param("parentId") String parentId,
            @Param("search") String search
    );

    boolean existsByOwnerIdAndNameAndParentId(String ownerId, String name, String parentId);

    long countByParentId(String parentId);
}
