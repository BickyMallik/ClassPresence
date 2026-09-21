package com.AttendPulse.attend_backend.controller;

import com.AttendPulse.attend_backend.entity.Department;
import com.AttendPulse.attend_backend.repository.DepartmentRepository;
import org.springframework.security.core.Authentication;
import com.AttendPulse.attend_backend.dto.AuthResponse;
import com.AttendPulse.attend_backend.dto.LoginRequest;
import com.AttendPulse.attend_backend.dto.RegisterRequest;
import com.AttendPulse.attend_backend.dto.StudentRegisterRequest;
import com.AttendPulse.attend_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.AttendPulse.attend_backend.entity.User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/student/register")
    public ResponseEntity<String> studentRegister(
            @Valid @RequestBody StudentRegisterRequest request) {
        return ResponseEntity.ok(authService.studentSelfRegister(request));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getProfile(Authentication auth) {
        return ResponseEntity.ok(authService.getUserByEmail(auth.getName()));
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(authService.forgotPassword(body.get("email")));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(authService.resetPassword(body.get("token"), body.get("newPassword")));
    }
}