package com.dormitory.management.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dormitory.management.entity.Student;
import com.dormitory.management.entity.enums.ContractStatus;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s WHERE LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Student> searchByCodeOrName(@Param("keyword") String keyword);

    @Query("SELECT s FROM Student s WHERE LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Student> searchByCodeOrName(@Param("keyword") String keyword, Pageable pageable);

        @Query("""
            SELECT DISTINCT s FROM Student s
            JOIN Contract c ON c.student = s
            WHERE c.status = :activeStatus
              AND (
              :keyword IS NULL OR :keyword = ''
              OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
        Page<Student> searchResidentStudents(
            @Param("keyword") String keyword,
            @Param("activeStatus") ContractStatus activeStatus,
            Pageable pageable);

    boolean existsByStudentCode(String studentCode);

    boolean existsByCccd(String cccd);

    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);

    boolean existsByCccdAndIdNot(String cccd, Long id);

    Optional<Student> findByStudentCodeIgnoreCase(String studentCode);
}