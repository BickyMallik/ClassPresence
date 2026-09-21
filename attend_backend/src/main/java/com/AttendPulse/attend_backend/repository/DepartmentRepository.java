package com.AttendPulse.attend_backend.repository;

import com.AttendPulse.attend_backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
