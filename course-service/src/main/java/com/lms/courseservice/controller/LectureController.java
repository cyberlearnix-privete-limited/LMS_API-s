package com.lms.courseservice.controller;

import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.service.LectureService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    // ✅ Instructor/Admin only
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PostMapping("/{sectionId}/lectures")
    public Lecture createLecture(@PathVariable Long sectionId,
                                 @RequestBody Lecture lecture) {
        return lectureService.createLecture(sectionId, lecture);
    }

    // ✅ Instructor/Admin only
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PatchMapping("/{sectionId}/lectures/{lectureId}")
    public Lecture updateLecture(@PathVariable Long sectionId,
                                 @PathVariable Long lectureId,
                                 @RequestBody Lecture lecture) {

        return lectureService.updateLecture(lectureId, lecture);
    }

    // ✅ Instructor/Admin only
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @DeleteMapping("/{sectionId}/lectures/{lectureId}")
    public Map<String, String> deleteLecture(@PathVariable Long sectionId,
                                             @PathVariable Long lectureId) {
        lectureService.deleteLecture(sectionId, lectureId);
        return Map.of("message", "Lecture deleted successfully");
    }

    // 🔒 Only enrolled users (handled in service)
    @GetMapping("/{sectionId}/lectures")
    public List<Lecture> getLectures(@PathVariable Long sectionId) {
        return lectureService.getLecturesBySection(sectionId);
    }
}