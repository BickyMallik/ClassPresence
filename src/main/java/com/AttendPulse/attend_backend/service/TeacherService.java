package com.AttendPulse.attend_backend.service;

import com.AttendPulse.attend_backend.entity.AttendanceRecord;
import com.AttendPulse.attend_backend.entity.User;
import com.AttendPulse.attend_backend.dto.AttendanceSessionRequest;
import com.AttendPulse.attend_backend.dto.StudentRequest;
import com.AttendPulse.attend_backend.dto.SubjectRequest;
import com.AttendPulse.attend_backend.entity.*;
import com.AttendPulse.attend_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class TeacherService {

    @Autowired private UserRepository userRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private AttendanceSessionRepository sessionRepository;
    @Autowired private AttendanceRecordRepository recordRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;


    public Subject addSubject(SubjectRequest request, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found!"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found!"));

        Subject subject = new Subject();
        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setDepartment(department);
        subject.setTeacher(teacher);
        subject.setSemester(request.getSemester());
        subject.setSessionLabel(request.getSessionLabel());

        return subjectRepository.save(subject);
    }


    public List<Subject> getMySubjects(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found!"));
        return subjectRepository.findByTeacherId(teacher.getId());
    }


    public Student addStudent(StudentRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.STUDENT);
        userRepository.save(user);

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found!"));

        Student student = new Student();
        student.setUser(user);
        student.setRollNo(request.getRollNo());
        student.setDepartment(department);
        student.setBatchYear(request.getBatchYear());

        return studentRepository.save(student);
    }

    public String enrollStudent(Long studentId, Long subjectId) {
        if (enrollmentRepository.existsByStudentIdAndSubjectId(studentId, subjectId)) {
            return "Student already enrolled!";
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found!"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found!"));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSubject(subject);
        enrollmentRepository.save(enrollment);
        return "Student enrolled successfully!";
    }

    public AttendanceSession startSession(AttendanceSessionRequest request, String teacherEmail) {

        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found!"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found!"));

        String otp = String.format("%06d", new Random().nextInt(999999));

        AttendanceSession session = new AttendanceSession();
        session.setSubject(subject);
        session.setOtpCode(otp);
        session.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        session.setIsLocked(false);
        session.setMaxCount(request.getMaxCount());
        session.setSessionDate(LocalDateTime.now());
        session.setTeacher(teacher);

        AttendanceSession savedSession = sessionRepository.save(session);

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subject.getId());

        for (Enrollment e : enrollments) {
            String email = e.getStudent().getUser().getEmail();
            String studentName = e.getStudent().getUser().getName();
            emailService.sendOtpEmail(email, studentName, otp, savedSession.getId());
        }

        return savedSession;
    }

    public String lockSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found!"));
        if (session.getIsLocked()) {
            return "Session already locked!";
        }
        session.setIsLocked(true);
        sessionRepository.save(session);
        return "Session locked!";
    }

    public List<AttendanceSession> getSessionsBySubject(Long subjectId) {
        return sessionRepository.findBySubjectId(subjectId);
    }

    public List<User> getPendingStudents() {
        return userRepository.findByRole(User.Role.STUDENT)
                .stream()
                .filter(u -> u.getStatus() == null || u.getStatus() == User.Status.PENDING)
                .toList();
    }

    public String approveStudent(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        user.setStatus(User.Status.APPROVED);
        userRepository.save(user);
        emailService.sendApprovalNotification(user.getEmail(), user.getName());
        return "Student approved!";
    }

    public String rejectStudent(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        user.setStatus(User.Status.REJECTED);
        userRepository.save(user);
        emailService.sendRejectionNotification(user.getEmail(), user.getName());
        return "Student rejected!";
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Subject> getAllSubjects(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found!"));
        return subjectRepository.findByTeacherId(teacher.getId());
    }

    public Map<String, Object> getWeekWiseAttendance(Long subjectId) {
        List<AttendanceSession> sessions = sessionRepository.findBySubjectId(subjectId);
        Map<String, Long> weekMap = new java.util.LinkedHashMap<>();

        for (AttendanceSession session : sessions) {
            if (session.getSessionDate() != null) {
                // Get week label like "Week 1", "Week 2"
                java.time.LocalDate date = session.getSessionDate().toLocalDate();
                int weekNum = date.get(java.time.temporal.WeekFields.ISO.weekOfYear());
                String weekLabel = "Week " + weekNum;
                long count = recordRepository.findBySessionId(session.getId()).size();
                weekMap.merge(weekLabel, count, Long::sum);
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("weekWise", weekMap);
        result.put("totalSessions", sessions.size());
        return result;
    }

    public List<Student> getStudentsBySubject(Long subjectId) {
        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subjectId);
        return enrollments.stream()
                .map(Enrollment::getStudent)
                .toList();
    }

    public List<AttendanceRecord> getFlaggedRecords(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found!"));
        List<Subject> subjects = subjectRepository.findByTeacherId(teacher.getId());
        List<AttendanceRecord> flagged = new java.util.ArrayList<>();
        for (Subject subject : subjects) {
            List<AttendanceSession> sessions = sessionRepository.findBySubjectId(subject.getId());
            for (AttendanceSession session : sessions) {
                List<AttendanceRecord> records = recordRepository.findBySessionId(session.getId());
                for (AttendanceRecord record : records) {
                    if (Boolean.TRUE.equals(record.getIsProxyFlagged())) {
                        flagged.add(record);
                    }
                }
            }
        }
        return flagged;
    }
}