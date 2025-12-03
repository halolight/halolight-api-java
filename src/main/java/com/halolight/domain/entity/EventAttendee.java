package com.halolight.domain.entity;

import com.halolight.domain.entity.enums.AttendeeStatus;
import com.halolight.domain.entity.id.EventAttendeeId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "event_attendees")
public class EventAttendee {

    @EmbeddedId
    @Builder.Default
    private EventAttendeeId id = new EventAttendeeId();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttendeeStatus status = AttendeeStatus.PENDING;

    @CreationTimestamp
    @Column(name = "responded_at")
    private Instant respondedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id", nullable = false)
    private CalendarEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
