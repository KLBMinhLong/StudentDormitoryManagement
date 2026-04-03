package com.dormitory.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dormitory.management.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByStudentId(Long studentId);

    @Query("SELECT au FROM AppUser au JOIN au.student s WHERE LOWER(s.studentCode) = LOWER(:studentCode)")
    Optional<AppUser> findByStudentCode(@Param("studentCode") String studentCode);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
