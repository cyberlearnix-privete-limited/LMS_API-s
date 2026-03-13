package com.lms.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lectures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String videoUrl;

    private Integer duration;

    private Integer orderIndex;

    private Boolean previewEnabled;

    private String resources;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;
}