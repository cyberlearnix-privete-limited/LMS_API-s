package com.user.register.repository;

import com.user.register.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);
    // Count registrations by email or mobile in the last 1 hour (for rate limiting)
    @Query("SELECT COUNT(u) FROM User u WHERE (u.email = :email OR u.mobile = :mobile) AND u.createdAt >= :after")
    long countByEmailOrMobileAndCreatedAtAfter(@Param("email") String email,
                                               @Param("mobile") String mobile,
                                               @Param("after") LocalDateTime after);
}