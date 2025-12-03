package com.halolight.domain.repository;

import com.halolight.domain.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {

    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<ActivityLog> findByResourceTypeAndResourceIdOrderByCreatedAtDesc(
            String resourceType,
            String resourceId,
            Pageable pageable
    );

    List<ActivityLog> findByAction(String action);

    @Query("SELECT a FROM ActivityLog a WHERE " +
            "a.createdAt >= :start AND a.createdAt <= :end " +
            "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByDateRange(
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );

    @Query("SELECT a FROM ActivityLog a WHERE " +
            "(:userId IS NULL OR a.userId = :userId) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:resourceType IS NULL OR a.resourceType = :resourceType) " +
            "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByFilters(
            @Param("userId") String userId,
            @Param("action") String action,
            @Param("resourceType") String resourceType,
            Pageable pageable
    );

    long countByUserId(String userId);

    long countByAction(String action);
}
