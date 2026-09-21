package com.AttendPulse.attend_backend.repository;

import com.AttendPulse.attend_backend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByDepartmentId(Long departmentId);
    List<Subject> findByTeacherId(Long teacherId);
}