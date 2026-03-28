package com.user.register.dto;


import com.user.register.entity.UserSession;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserSessionsResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private List<UserSession> sessions;

    public UserSessionsResponse( UUID userId, String email, String firstName, String lastName, List<UserSession> sessions) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sessions = sessions;
    }
}