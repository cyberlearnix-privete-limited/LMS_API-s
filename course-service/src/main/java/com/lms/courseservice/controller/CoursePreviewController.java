package com.lms.courseservice.controller;

import com.lms.courseservice.entity.CoursePreview;
import com.lms.courseservice.service.CoursePreviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CoursePreviewController {

    private final CoursePreviewService previewService;

    @PostMapping("/{courseId}/preview")
    public CoursePreview createPreview(@PathVariable Long courseId,
                                       @RequestBody CoursePreview preview){
        return previewService.createPreview(courseId, preview);
    }

    @GetMapping("/{courseId}/preview")
    public List<CoursePreview> getPreview(@PathVariable Long courseId){
        return previewService.getCoursePreview(courseId);
    }
}