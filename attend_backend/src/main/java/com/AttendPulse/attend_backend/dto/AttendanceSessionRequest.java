package com.AttendPulse.attend_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceSessionRequest {

    @NotNull
    private Long subjectId;

    @NotNull
    private Integer maxCount;

    private Double teacherLatitude;
    private Double teacherLongitude;
}