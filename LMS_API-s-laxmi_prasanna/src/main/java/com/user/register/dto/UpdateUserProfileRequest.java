package com.user.register.dto;

import lombok.Data;

@Data
public class UpdateUserProfileRequest {
    private String firstName;
    private String lastName;
    private String mobile;
    private String dob;
    private String profilePhoto;
    private String city;
    private String state;
    private String country;
    private String preferredLanguage;
    private String organization;
    private String skills;
    private String fieldOfStudy;
    private String highestQualification;
}