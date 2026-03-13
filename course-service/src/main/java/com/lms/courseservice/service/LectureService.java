package com.lms.courseservice.service;

import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.LectureRepository;
import com.lms.courseservice.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final SectionRepository sectionRepository;

    // Create Lecture
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

    // Get Lectures by Section
    public List<Lecture> getLecturesBySection(Long sectionId) {
        return lectureRepository.findBySectionId(sectionId);
    }

    // Update Lecture
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

    // Delete Lecture
    public void deleteLecture(Long sectionId, Long lectureId) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        if (!lecture.getSection().getId().equals(sectionId)) {
            throw new RuntimeException("Lecture does not belong to this section");
        }

        lectureRepository.delete(lecture);
    }
}