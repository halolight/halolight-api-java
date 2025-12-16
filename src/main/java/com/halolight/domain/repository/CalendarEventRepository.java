package com.halolight.domain.repository;

import com.halolight.domain.entity.CalendarEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, String> {

    Page<CalendarEvent> findByOrganizerId(String organizerId, Pageable pageable);

    Page<CalendarEvent> findByTeamId(String teamId, Pageable pageable);

    @Query("SELECT e FROM CalendarEvent e WHERE " +
            "e.organizerId = :userId AND " +
            "e.startTime >= :start AND e.endTime <= :end")
    List<CalendarEvent> findByOrganizerIdAndDateRange(
            @Param("userId") String userId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT e FROM CalendarEvent e " +
            "LEFT JOIN e.attendees ea " +
            "WHERE (e.organizerId = :userId OR ea.user.id = :userId) " +
            "AND e.startTime >= :start AND e.endTime <= :end")
    List<CalendarEvent> findByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT e FROM CalendarEvent e WHERE " +
            "e.teamId = :teamId AND " +
            "e.startTime >= :start AND e.endTime <= :end")
    List<CalendarEvent> findByTeamIdAndDateRange(
            @Param("teamId") String teamId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT e FROM CalendarEvent e WHERE " +
            "e.startTime >= :now ORDER BY e.startTime ASC")
    List<CalendarEvent> findUpcomingEvents(@Param("now") Instant now, Pageable pageable);
}
