package com.user.register.repository;

import com.user.register.entity.UserSession;
import com.user.register.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUser(User user);
    void deleteByUser(User user);

    // ✅ Add this method to find a session by its token
    Optional<UserSession> findByToken(String token);
    Optional<UserSession> findByAccessToken(String token);
    Optional<UserSession> findByRefreshToken(String token);
}