package com.dormitory.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dormitory.management.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByTokenAndUsedTrue(String token);

    void deleteByAppUser_Id(Long appUserId);
}
