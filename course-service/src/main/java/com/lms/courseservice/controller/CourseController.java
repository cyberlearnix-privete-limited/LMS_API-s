package com.lms.courseservice.controller;

import com.lms.courseservice.entity.Course;
import com.lms.courseservice.security.JwtUtil;
import com.lms.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final JwtUtil jwtUtil;

    // Instructor/Admin only
    @PostMapping
    public Course createCourse(@RequestBody Course course){
        return courseService.createCourse(course);
    }

    // Public
    @GetMapping
    public List<Course> getAllCourses(){
        return courseService.getAllCourses();
    }

    // Public
    @GetMapping("/{id}")
    public Course getCourse(@PathVariable Long id){
        return courseService.getCourseById(id);
    }

    // Full update
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Course course){
        return courseService.updateCourse(id, course);
    }

    // Partial update
    @PatchMapping("/{id}")
    public Course updateCoursePartial(@PathVariable Long id, @RequestBody Course course){
        return courseService.updateCourse(id, course);
    }

    // Instructor/Admin only
    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable Long id){
        courseService.deleteCourse(id);
    }

    // 🔥 Get students enrolled in course
    @GetMapping("/{courseId}/students")
    public List<UUID> getStudents(@PathVariable Long courseId){
        return courseService.getStudents(courseId);
    }

    // 🔥 Enroll user
    @PostMapping("/{courseId}/enroll")
    public String enroll(@PathVariable Long courseId,
                         @RequestHeader("Authorization") String token) {

        String tokenValue = token.substring(7);

        UUID userId = jwtUtil.extractUserId(tokenValue);

        courseService.enrollUser(courseId, userId); // ✅ FIXED

        return "Enrolled successfully";
    }
}