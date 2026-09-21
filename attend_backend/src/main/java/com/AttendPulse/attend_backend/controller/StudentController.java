package com.AttendPulse.attend_backend.controller;

import com.AttendPulse.attend_backend.dto.MarkAttendanceRequest;
import com.AttendPulse.attend_backend.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/attendance/mark")
    public ResponseEntity<String> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request,
            Authentication auth,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(
                studentService.markAttendance(request, auth.getName(), ip));
    }

    @GetMapping("/attendance/overall")
    public ResponseEntity<Map<String, Object>> getOverallAttendance(Authentication auth) {
        return ResponseEntity.ok(
                studentService.getOverallAttendance(auth.getName()));
    }

    @GetMapping("/attendance/{subjectId}")
    public ResponseEntity<Map<String, Object>> getSubjectAttendance(
            @PathVariable Long subjectId,
            Authentication auth) {
        return ResponseEntity.ok(
                studentService.getSubjectAttendance(auth.getName(), subjectId));
    }
}