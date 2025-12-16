package com.halolight.web.dto.calendar;

import jakarta.validation.Valid;
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
public class UpdateEventRequest {

    private String title;

    private String description;

    private Instant startTime;

    private Instant endTime;

    private Boolean allDay;

    private String location;

    private String color;

    private String teamId;

    @Valid
    private List<AttendeeRequest> attendees;

    @Valid
    private List<ReminderRequest> reminders;
}
