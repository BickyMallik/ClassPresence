package com.AttendPulse.attend_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubjectRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    @NotNull
    private Long departmentId;

    @NotNull
    private Integer semester;

    @NotBlank
    private String sessionLabel;
}
