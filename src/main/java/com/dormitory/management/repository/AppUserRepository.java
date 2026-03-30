package com.dormitory.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dormitory.management.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByStudentId(Long studentId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
