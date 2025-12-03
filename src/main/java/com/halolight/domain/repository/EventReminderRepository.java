package com.halolight.domain.repository;

import com.halolight.domain.entity.EventReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventReminderRepository extends JpaRepository<EventReminder, String> {

    List<EventReminder> findByEventId(String eventId);

    List<EventReminder> findBySentFalseAndRemindAtBefore(Instant now);

    void deleteByEventId(String eventId);

    long countByEventId(String eventId);
}
