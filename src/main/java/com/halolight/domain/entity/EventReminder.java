package com.halolight.domain.entity;

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
@Table(name = "event_reminders", indexes = {
        @Index(name = "idx_reminders_event", columnList = "event_id"),
        @Index(name = "idx_reminders_remind_at", columnList = "remind_at")
})
public class EventReminder {

    @Id
    @Column(nullable = false, updatable = false, length = 40)
    private String id;

    @Column(name = "event_id", nullable = false, length = 40)
    private String eventId;

    @Column(name = "remind_at", nullable = false)
    private Instant remindAt;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String type = "email";

    @Column(nullable = false)
    @Builder.Default
    private Boolean sent = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private CalendarEvent event;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 25);
        }
    }
}
