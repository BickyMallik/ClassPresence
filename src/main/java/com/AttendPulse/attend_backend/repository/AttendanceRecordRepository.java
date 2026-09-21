package com.AttendPulse.attend_backend.repository;

import com.AttendPulse.attend_backend.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByStudentId(Long studentId);
    List<AttendanceRecord> findBySessionId(Long sessionId);
    boolean existsByStudentIdAndSessionId(Long studentId, Long sessionId);
    long countBySessionIdAndIpAddress(Long sessionId, String ipAddress);
    long countBySessionIdAndDeviceFingerprint(Long sessionId, String deviceFingerprint);
    long countBySessionIdAndMarkedAtAfter(Long sessionId, LocalDateTime time);
    long countBySessionId(Long sessionId);
    List<AttendanceRecord> findByStudentIdAndSession_Subject_Id(Long studentId, Long subjectId);
    List<AttendanceRecord> findBySessionIdAndMarkedAtBetween(
            Long sessionId, LocalDateTime start, LocalDateTime end);
}