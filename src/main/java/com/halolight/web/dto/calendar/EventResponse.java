package com.halolight.web.dto.calendar;

import com.halolight.domain.entity.enums.AttendeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private String id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Boolean allDay;
    private String location;
    private String color;
    private String organizerId;
    private String teamId;
    private Instant createdAt;
    private Instant updatedAt;
    private OrganizerInfo organizer;
    private TeamInfo team;
    private List<AttendeeInfo> attendees;
    private List<ReminderInfo> reminders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizerInfo {
        private String id;
        private String username;
        private String name;
        private String email;
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamInfo {
        private String id;
        private String name;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendeeInfo {
        private String userId;
        private String username;
        private String name;
        private String email;
        private String avatar;
        private AttendeeStatus status;
        private Instant respondedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReminderInfo {
        private String id;
        private Instant remindAt;
        private String type;
        private Boolean sent;
        private Instant createdAt;
    }
}
