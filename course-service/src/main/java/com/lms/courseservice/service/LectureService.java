package com.lms.courseservice.service;

import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.LectureRepository;
import com.lms.courseservice.repository.SectionRepository;
import com.lms.courseservice.repository.EnrollmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SectionService sectionService;

    // 🔒 Common method to validate enrollment

    private void validateEnrollment(Long sectionId) {

        // 🔥 Get UUID from JWT
        String userId = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        UUID studentId = UUID.fromString(userId);

        Long courseId = sectionService.getCourseIdBySection(sectionId);

        boolean enrolled = enrollmentRepository
                .existsByStudentIdAndCourseId(studentId, courseId);

        if (!enrolled) {
            throw new AccessDeniedException("You are not enrolled in this course");
        }
    }

    // ✅ Create Lecture
    public Lecture createLecture(Long sectionId, Lecture lecture) {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        // Prevent duplicate lecture title in same section
        lectureRepository.findByTitleAndSectionId(lecture.getTitle(), sectionId)
                .ifPresent(l -> {
                    throw new RuntimeException("Lecture with this title already exists in this section");
                });

        lecture.setSection(section);

        return lectureRepository.save(lecture);
    }

    // 🔒 Get Lectures by Section (ONLY ENROLLED USERS)
    public List<Lecture> getLecturesBySection(Long sectionId) {

        validateEnrollment(sectionId);

        return lectureRepository.findBySectionId(sectionId);
    }

    // 🔒 Update Lecture (OPTIONAL: restrict to enrolled or admin/instructor)
    public Lecture updateLecture(Long lectureId, Lecture updatedLecture) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        if (updatedLecture.getTitle() != null)
            lecture.setTitle(updatedLecture.getTitle());

        if (updatedLecture.getDescription() != null)
            lecture.setDescription(updatedLecture.getDescription());

        if (updatedLecture.getVideoUrl() != null)
            lecture.setVideoUrl(updatedLecture.getVideoUrl());

        if (updatedLecture.getDuration() != null)
            lecture.setDuration(updatedLecture.getDuration());

        return lectureRepository.save(lecture);
    }

    // 🔒 Delete Lecture
    public void deleteLecture(Long sectionId, Long lectureId) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        if (!lecture.getSection().getId().equals(sectionId)) {
            throw new RuntimeException("Lecture does not belong to this section");
        }

        // ❌ REMOVE enrollment validation here

        lectureRepository.delete(lecture);
    }
}