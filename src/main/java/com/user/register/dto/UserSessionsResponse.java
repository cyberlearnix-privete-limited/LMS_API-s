package com.user.register.dto;


import com.user.register.entity.UserSession;
import lombok.Data;

import java.util.List;

@Data
public class UserSessionsResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private List<UserSession> sessions;

    public UserSessionsResponse(Long userId, String email, String firstName, String lastName, List<UserSession> sessions) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sessions = sessions;
    }
}