package com.lms.courseservice.controller;

import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // Instructor/Admin only
    @PostMapping
    public Course createCourse(@RequestBody Course course){
        return courseService.createCourse(course);
    }

    @GetMapping
    public List<Course> getAllCourses(){
        return courseService.getAllCourses();
    }

    // Public
    @GetMapping("/{id}")
    public Course getCourse(@PathVariable Long id){
        return courseService.getCourseById(id);
    }

    // Full update (optional)
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Course course){
        return courseService.updateCourse(id, course);
    }

    // Partial update (recommended)
    @PatchMapping("/{id}")
    public Course updateCoursePartial(@PathVariable Long id, @RequestBody Course course){
        return courseService.updateCourse(id, course);
    }

    // Instructor/Admin only
    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable Long id){
        courseService.deleteCourse(id);
    }
}