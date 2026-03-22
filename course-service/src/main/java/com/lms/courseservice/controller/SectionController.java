package com.lms.courseservice.controller;

import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.security.JwtUtil;
import com.lms.courseservice.service.LectureService;
import com.lms.courseservice.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    private final JwtUtil jwtUtil;
    private final LectureService lectureService;
    private final EnrollmentRepository enrollmentRepository;

    // Instructor/Admin only
    @PostMapping("/{courseId}/sections")
    public Section createSection(@PathVariable Long courseId,
                                 @RequestBody Section section) {
        return sectionService.createSection(courseId, section);
    }

    // Everyone can view
    @GetMapping("/{courseId}/sections")
    public List<Section> getSections(@PathVariable Long courseId) {
        return sectionService.getSectionsByCourseId(courseId);
    }

    // Instructor/Admin only
    @PatchMapping("/sections/{sectionId}")
    public Section updateSection(@PathVariable Long sectionId,
                                 @RequestBody Section section) {
        return sectionService.updateSection(sectionId, section);
    }

    // Instructor/Admin only
    @DeleteMapping("/sections/{sectionId}")
    public void deleteSection(@PathVariable Long sectionId) {
        sectionService.deleteSection(sectionId);
    }

}