package com.AttendPulse.attend_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private String otpCode;
    private LocalDateTime otpExpiresAt;
    private Boolean isLocked;
    private Integer maxCount;
    private LocalDateTime sessionDate;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;
}
