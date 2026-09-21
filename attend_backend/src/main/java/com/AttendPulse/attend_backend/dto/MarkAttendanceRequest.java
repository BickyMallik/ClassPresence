package com.AttendPulse.attend_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MarkAttendanceRequest {

    @NotNull
    private Long sessionId;

    @NotBlank
    private String otpCode;

    @NotBlank
    private String deviceFingerprint;

    private Double studentLatitude;
    private Double studentLongitude;
}