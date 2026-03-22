package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentIdAndCourseId(UUID studentId, Long courseId);
    List<Enrollment> findByCourseId(Long courseId);
}