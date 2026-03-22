package com.lms.courseservice.service;

import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;

    // Create Section
    public Section createSection(Long courseId, Section section) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        section.setCourse(course);

        return sectionRepository.save(section);
    }

    // Get Sections by Course
    public List<Section> getSectionsByCourseId(Long courseId) {
        return sectionRepository.findByCourseId(courseId);
    }

    // Update Section
    public Section updateSection(Long sectionId, Section updatedSection) {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        if (updatedSection.getTitle() != null)
            section.setTitle(updatedSection.getTitle());

        if (updatedSection.getOrderIndex() != null)
            section.setOrderIndex(updatedSection.getOrderIndex());

        return sectionRepository.save(section);
    }

    public Long getCourseIdBySection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        return section.getCourse().getId();
    }
    // Delete Section

    public void deleteSection(Long sectionId) {
        sectionRepository.deleteById(sectionId);
    }
}