package com.halolight.domain.repository;

import com.halolight.domain.entity.EventAttendee;
import com.halolight.domain.entity.enums.AttendeeStatus;
import com.halolight.domain.entity.id.EventAttendeeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, EventAttendeeId> {

    List<EventAttendee> findByIdEventId(String eventId);

    List<EventAttendee> findByIdUserId(String userId);

    List<EventAttendee> findByIdEventIdAndStatus(String eventId, AttendeeStatus status);

    boolean existsByIdEventIdAndIdUserId(String eventId, String userId);

    void deleteByIdEventId(String eventId);

    long countByIdEventId(String eventId);

    long countByIdEventIdAndStatus(String eventId, AttendeeStatus status);
}
