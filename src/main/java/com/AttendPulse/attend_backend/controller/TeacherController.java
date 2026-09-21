package com.AttendPulse.attend_backend.controller;

import com.AttendPulse.attend_backend.entity.AttendanceRecord;
import com.AttendPulse.attend_backend.entity.*;
import com.AttendPulse.attend_backend.dto.AttendanceSessionRequest;
import com.AttendPulse.attend_backend.dto.StudentRequest;
import com.AttendPulse.attend_backend.dto.SubjectRequest;
import com.AttendPulse.attend_backend.service.PdfService;
import com.AttendPulse.attend_backend.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private PdfService pdfService;

    @PostMapping("/subject")
    public ResponseEntity<Subject> addSubject(@Valid @RequestBody SubjectRequest request,
                                              Authentication auth) {
        return ResponseEntity.ok(teacherService.addSubject(request, auth.getName()));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getMySubjects(Authentication auth) {
        return ResponseEntity.ok(teacherService.getMySubjects(auth.getName()));
    }

    @PostMapping("/student")
    public ResponseEntity<Student> addStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(teacherService.addStudent(request));
    }

    @PostMapping("/enroll/{studentId}/{subjectId}")
    public ResponseEntity<String> enrollStudent(@PathVariable Long studentId,
                                                @PathVariable Long subjectId) {
        return ResponseEntity.ok(teacherService.enrollStudent(studentId, subjectId));
    }

    @PostMapping("/session/start")
    public ResponseEntity<AttendanceSession> startSession(
            @Valid @RequestBody AttendanceSessionRequest request,
            Authentication auth) {
        return ResponseEntity.ok(teacherService.startSession(request, auth.getName()));
    }

    @PutMapping("/session/lock/{sessionId}")
    public ResponseEntity<String> lockSession(@PathVariable Long sessionId) {
        try {
            return ResponseEntity.ok(teacherService.lockSession(sessionId));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(e.getMessage());
        }
    }

    @GetMapping("/session/{subjectId}")
    public ResponseEntity<List<AttendanceSession>> getSessions(@PathVariable Long subjectId) {
        return ResponseEntity.ok(teacherService.getSessionsBySubject(subjectId));
    }

    @GetMapping("/students/pending")
    public ResponseEntity<List<User>> getPendingStudents() {
        return ResponseEntity.ok(teacherService.getPendingStudents());
    }

    @PutMapping("/students/approve/{userId}")
    public ResponseEntity<String> approveStudent(@PathVariable Long userId) {
        return ResponseEntity.ok(teacherService.approveStudent(userId));
    }

    @PutMapping("/students/reject/{userId}")
    public ResponseEntity<String> rejectStudent(@PathVariable Long userId) {
        return ResponseEntity.ok(teacherService.rejectStudent(userId));
    }

    @GetMapping("/students/all")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(teacherService.getAllStudents());
    }

    @GetMapping("/subjects/all")
    public ResponseEntity<List<Subject>> getAllSubjects(Authentication auth) {
        return ResponseEntity.ok(teacherService.getAllSubjects(auth.getName()));
    }

    @GetMapping("/attendance/weekly/{subjectId}")
    public ResponseEntity<Map<String, Object>> getWeekWiseAttendance(
            @PathVariable Long subjectId) {
        return ResponseEntity.ok(teacherService.getWeekWiseAttendance(subjectId));
    }

    @GetMapping("/report/pdf/{subjectId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long subjectId) {
        try {
            byte[] pdf = pdfService.generateAttendanceReport(subjectId);
            return ResponseEntity.ok()
                    .header("Content-Disposition",
                            "attachment; filename=attendance_report.pdf")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<AttendanceRecord>> getFlaggedRecords(Authentication auth) {
        return ResponseEntity.ok(teacherService.getFlaggedRecords(auth.getName()));
    }
}