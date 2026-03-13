package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByCourseId(Long courseId);

}