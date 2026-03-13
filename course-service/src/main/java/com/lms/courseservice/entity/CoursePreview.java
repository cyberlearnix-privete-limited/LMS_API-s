package com.lms.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_previews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePreview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String videoUrl;

    private Integer duration;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}