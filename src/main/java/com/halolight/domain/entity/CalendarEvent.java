package com.halolight.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "calendar_events", indexes = {
        @Index(name = "idx_events_organizer", columnList = "organizer_id"),
        @Index(name = "idx_events_team", columnList = "team_id"),
        @Index(name = "idx_events_start", columnList = "start_time"),
        @Index(name = "idx_events_end", columnList = "end_time")
})
public class CalendarEvent {

    @Id
    @Column(nullable = false, updatable = false, length = 40)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "all_day", nullable = false)
    @Builder.Default
    private Boolean allDay = false;

    private String location;

    @Column(length = 20)
    private String color;

    @Column(name = "organizer_id", nullable = false, length = 40)
    private String organizerId;

    @Column(name = "team_id", length = 40)
    private String teamId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", insertable = false, updatable = false)
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventAttendee> attendees = new LinkedHashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventReminder> reminders = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 25);
        }
        if (this.organizerId == null && this.organizer != null) {
            this.organizerId = this.organizer.getId();
        }
        if (this.teamId == null && this.team != null) {
            this.teamId = this.team.getId();
        }
    }
}
