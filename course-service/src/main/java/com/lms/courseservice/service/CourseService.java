package com.lms.courseservice.service;

import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;

    public Course createCourse(Course course) {
        course.setStatus("Draft");
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    public Course updateCourse(Long id, Course updatedCourse) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        if (updatedCourse.getTitle() != null)
            course.setTitle(updatedCourse.getTitle());

        if (updatedCourse.getSubtitle() != null)
            course.setSubtitle(updatedCourse.getSubtitle());

        if (updatedCourse.getDescription() != null)
            course.setDescription(updatedCourse.getDescription());

        if (updatedCourse.getCategory() != null)
            course.setCategory(updatedCourse.getCategory());

        if (updatedCourse.getLevel() != null)
            course.setLevel(updatedCourse.getLevel());

        if (updatedCourse.getLanguage() != null)
            course.setLanguage(updatedCourse.getLanguage());

        if (updatedCourse.getPrice() != null)
            course.setPrice(updatedCourse.getPrice());

        if (updatedCourse.getThumbnail() != null)
            course.setThumbnail(updatedCourse.getThumbnail());

        if (updatedCourse.getStatus() != null)
            course.setStatus(updatedCourse.getStatus());

        if (updatedCourse.getInstructorId() != null)
            course.setInstructorId(updatedCourse.getInstructorId());

        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
    public Lecture enableLecturePreview(Long courseId, Long lectureId){

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        if(!lecture.getSection().getCourse().getId().equals(courseId)){
            throw new RuntimeException("Lecture does not belong to this course");
        }

        lecture.setPreviewEnabled(true);

        return lectureRepository.save(lecture);
    }
}