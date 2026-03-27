package com.user.register.repository;

import com.user.register.entity.OTPCode;
import com.user.register.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OTPCodeRepository extends JpaRepository<OTPCode, UUID> {

    Optional<OTPCode> findByUserAndTypeAndOtp(User user, String type, String otp);
    void deleteByUserAndType(User user, String type);
    Optional<OTPCode> findTopByUserAndTypeOrderByCreatedAtDesc(User user, String otpType);

    void deleteByUser(User user);
}