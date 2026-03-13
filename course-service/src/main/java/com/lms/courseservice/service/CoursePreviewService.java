package com.lms.courseservice.service;

import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.CoursePreview;
import com.lms.courseservice.repository.CoursePreviewRepository;
import com.lms.courseservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursePreviewService {

    private final CoursePreviewRepository previewRepository;
    private final CourseRepository courseRepository;

    public CoursePreview createPreview(Long courseId, CoursePreview preview){

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        preview.setCourse(course);

        return previewRepository.save(preview);
    }

    public List<CoursePreview> getCoursePreview(Long courseId){
        return previewRepository.findByCourseId(courseId);
    }
}