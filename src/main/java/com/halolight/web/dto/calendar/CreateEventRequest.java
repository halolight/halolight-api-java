package com.halolight.web.dto.calendar;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateEventRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "开始时间不能为空")
    private Instant startTime;

    @NotNull(message = "结束时间不能为空")
    private Instant endTime;

    @Builder.Default
    private Boolean allDay = false;

    private String location;

    private String color;

    private String teamId;

    @Valid
    private List<AttendeeRequest> attendees;

    @Valid
    private List<ReminderRequest> reminders;
}
