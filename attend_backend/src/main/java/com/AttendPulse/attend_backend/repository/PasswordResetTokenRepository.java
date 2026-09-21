package com.AttendPulse.attend_backend.repository;

import com.AttendPulse.attend_backend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
