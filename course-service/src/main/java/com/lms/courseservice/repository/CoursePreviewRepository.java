package com.lms.courseservice.repository;

import com.lms.courseservice.entity.CoursePreview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoursePreviewRepository extends JpaRepository<CoursePreview, Long> {

    List<CoursePreview> findByCourseId(Long courseId);

}