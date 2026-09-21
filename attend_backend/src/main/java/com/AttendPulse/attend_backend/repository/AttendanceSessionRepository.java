package com.AttendPulse.attend_backend.repository;

import com.AttendPulse.attend_backend.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findByOtpCode(String otpCode);
    List<AttendanceSession> findBySubjectId(Long subjectId);
}