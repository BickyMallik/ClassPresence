package com.AttendPulse.attend_backend.service;

import com.AttendPulse.attend_backend.dto.AuthResponse;
import com.AttendPulse.attend_backend.dto.LoginRequest;
import com.AttendPulse.attend_backend.dto.RegisterRequest;
import com.AttendPulse.attend_backend.dto.StudentRegisterRequest;
import com.AttendPulse.attend_backend.entity.Department;
import com.AttendPulse.attend_backend.entity.Student;
import com.AttendPulse.attend_backend.entity.User;
import com.AttendPulse.attend_backend.repository.DepartmentRepository;
import com.AttendPulse.attend_backend.repository.StudentRepository;
import com.AttendPulse.attend_backend.repository.UserRepository;
import com.AttendPulse.attend_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EmailService emailService;

    public AuthResponse register(RegisterRequest request){
        System.out.println("=== REGISTER CALLED ===");
        System.out.println("Email: " + request.getEmail());
        System.out.println("Total users in DB: " + userRepository.count());

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email Already Registered!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getName());
    }

    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(()-> new RuntimeException("User not found!"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getName());
    }

    public String studentSelfRegister(StudentRegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        if (!request.getPassword().matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,}$")) {
            throw new RuntimeException("Weak password!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.Status.PENDING);
        userRepository.save(user);

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found!"));

        Student student = new Student();
        student.setUser(user);
        student.setRollNo(request.getRollNo());
        student.setDepartment(department);
        student.setBatchYear(request.getBatchYear());
        studentRepository.save(student);

        emailService.sendRegistrationPending(user.getEmail(), user.getName());

        return "Registration successful! Wait for approval.";
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }
}
