package com.AttendPulse.attend_backend.repository;

import com.AttendPulse.attend_backend.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findBySubjectId(Long subjectId);
    boolean existsByStudentIdAndSubjectId(Long studentId, Long subjectId);
}