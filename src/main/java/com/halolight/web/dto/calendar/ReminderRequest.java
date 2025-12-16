package com.halolight.web.dto.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderRequest {

    @NotNull(message = "提醒时间不能为空")
    private Instant remindAt;

    @NotBlank(message = "提醒类型不能为空")
    @Builder.Default
    private String type = "email";
}
