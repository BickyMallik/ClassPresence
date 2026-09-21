package com.AttendPulse.attend_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String rollNo;

    @NotNull
    private Long departmentId;

    @NotNull
    private Integer batchYear;
}