package com.lms.courseservice.controller;

import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    // Instructor/Admin
    @PostMapping("/{sectionId}/lectures")
    public Lecture createLecture(@PathVariable Long sectionId,
                                 @RequestBody Lecture lecture) {
        return lectureService.createLecture(sectionId, lecture);
    }

    // Everyone can view lectures of a section
    @GetMapping("/{sectionId}/lectures")
    public List<Lecture> getLectures(@PathVariable Long sectionId) {
        return lectureService.getLecturesBySection(sectionId);
    }

    // Instructor/Admin
    @PatchMapping("/{sectionId}/lectures/{lectureId}")
    public Lecture updateLecture(@PathVariable Long lectureId,
                                 @RequestBody Lecture lecture) {
        return lectureService.updateLecture(lectureId, lecture);
    }

    // Instructor/Admin
    @DeleteMapping("/{sectionId}/lectures/{lectureId}")
    public Map<String, String> deleteLecture(@PathVariable Long sectionId,
                                             @PathVariable Long lectureId) {
        lectureService.deleteLecture(sectionId, lectureId);
        return Map.of("message", "Lecture deleted successfully");

    }
}