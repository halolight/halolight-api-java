package com.halolight.web.dto.calendar;

import com.halolight.domain.entity.enums.AttendeeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttendeeStatusRequest {

    @NotNull(message = "状态不能为空")
    private AttendeeStatus status;
}
