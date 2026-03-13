package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    List<Lecture> findBySectionId(Long sectionId);

    Optional<Lecture> findByTitleAndSectionId(String title, Long sectionId);

    @Query("""
            SELECT l FROM Lecture l
            WHERE l.section.course.id = :courseId
            AND l.previewEnabled = true
            """)
    List<Lecture> findPreviewLecturesByCourseId(Long courseId);

}