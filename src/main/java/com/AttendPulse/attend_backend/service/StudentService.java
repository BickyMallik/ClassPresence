package com.AttendPulse.attend_backend.service;

import com.AttendPulse.attend_backend.dto.MarkAttendanceRequest;
import com.AttendPulse.attend_backend.entity.*;
import com.AttendPulse.attend_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private AttendanceSessionRepository sessionRepository;
    @Autowired private AttendanceRecordRepository recordRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private EmailService emailService;

    public String markAttendance(MarkAttendanceRequest request,
                                 String studentEmail,
                                 String ipAddress) {

        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student not found!"));

        // ✅ Use sessionId instead of OTP lookup
        AttendanceSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found!"));

        // ✅ OTP validation
        if (!session.getOtpCode().equals(request.getOtpCode())) {
            return "Invalid OTP!";
        }

        if (LocalDateTime.now().isAfter(session.getOtpExpiresAt())) {
            return "OTP expired!";
        }

        if (session.getIsLocked()) {
            return "Session is locked!";
        }

        // ✅ ENROLLMENT CHECK (IMPORTANT)
        boolean enrolled = enrollmentRepository
                .existsByStudentIdAndSubjectId(student.getId(), session.getSubject().getId());

        if (!enrolled) {
            return "You are not enrolled in this subject!";
        }

        if (recordRepository.existsByStudentIdAndSessionId(student.getId(), session.getId())) {
            return "Attendance already marked!";
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setStudent(student);
        record.setSession(session);
        record.setIpAddress(ipAddress);
        record.setDeviceFingerprint(request.getDeviceFingerprint());
        LocalDateTime markedAt = LocalDateTime.now();
        record.setMarkedAt(markedAt);
        boolean proxyFlagged = isProxySuspected(
                ipAddress,
                request.getDeviceFingerprint(),
                session.getId(),
                markedAt
        );
        record.setIsProxyFlagged(proxyFlagged);

        recordRepository.save(record);

        if (session.getMaxCount() != null && session.getMaxCount() > 0) {
            long markedCount = recordRepository.countBySessionId(session.getId());
            if (markedCount >= session.getMaxCount()) {
                session.setIsLocked(true);
                sessionRepository.save(session);
            }
        }

        return "Attendance marked successfully!";
    }

    private boolean isProxySuspected(String ip, String deviceId,
                                     Long sessionId, LocalDateTime markedAt) {
        // Rule 1: Same IP used before in this session
//        if (recordRepository.countBySessionIdAndIpAddress(sessionId, ip) > 0) {
//            return true;
//        }
        // Rule 2: Same device fingerprint used before
        if (recordRepository.countBySessionIdAndDeviceFingerprint(sessionId, deviceId) > 0) {
            return true;
        }
        // Rule 3: More than 3 submissions in last 3 seconds
        LocalDateTime window = markedAt.minusSeconds(3);
        if (recordRepository.countBySessionIdAndMarkedAtAfter(sessionId, window) > 3) {
            return true;
        }
        return false;
    }

    public Map<String, Object> getSubjectAttendance(String studentEmail, Long subjectId) {
        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student not found!"));

        List<AttendanceRecord> records = recordRepository
                .findByStudentIdAndSession_Subject_Id(student.getId(), subjectId);

        List<AttendanceSession> allSessions = sessionRepository.findBySubjectId(subjectId);

        int totalSessions = allSessions.size();
        int attended = records.size();
        double percentage = totalSessions == 0 ? 0 :
                ((double) attended / totalSessions) * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("totalSessions", totalSessions);
        result.put("attended", attended);
        result.put("percentage", Math.round(percentage * 100.0) / 100.0);
        return result;
    }

    public Map<String, Object> getOverallAttendance(String studentEmail) {
        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student not found!"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());

        int totalSessions = 0;
        int totalAttended = 0;

        for (Enrollment enrollment : enrollments) {
            Long subjectId = enrollment.getSubject().getId();
            List<AttendanceSession> sessions = sessionRepository.findBySubjectId(subjectId);
            List<AttendanceRecord> records = recordRepository
                    .findByStudentIdAndSession_Subject_Id(student.getId(), subjectId);
            totalSessions += sessions.size();
            totalAttended += records.size();
        }

        double percentage = totalSessions == 0 ? 0 :
                ((double) totalAttended / totalSessions) * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("totalSessions", totalSessions);
        result.put("totalAttended", totalAttended);
        result.put("overallPercentage", Math.round(percentage * 100.0) / 100.0);
        return result;
    }
}