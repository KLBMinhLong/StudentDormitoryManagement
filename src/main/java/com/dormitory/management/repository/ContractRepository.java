package com.dormitory.management.repository;

import com.dormitory.management.entity.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dormitory.management.entity.Contract;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByIdAndStudentId(Long id, Long studentId);

    Optional<Contract> findFirstByStudentIdAndStatusOrderByCreatedAtDesc(Long studentId, ContractStatus status);

    Optional<Contract> findFirstByBedIdAndStatusInOrderByCreatedAtDesc(Long bedId, Set<ContractStatus> statuses);

    boolean existsByStudentIdAndStatusIn(Long studentId, Set<ContractStatus> statuses);

    List<Contract> findByStatusAndHoldExpiresAtBefore(ContractStatus status, LocalDateTime time);

    List<Contract> findByStatusAndEndDateBefore(ContractStatus status, LocalDate endDate);

    List<Contract> findByStatusOrderByCreatedAtDesc(ContractStatus status);

    // Lấy danh sách hợp đồng theo phòng
    List<Contract> findByRoomId(Long roomId);

    List<Contract> findByRoomIdAndStatus(Long roomId, ContractStatus status);

    // Lấy danh sách hợp đồng theo mã sinh viên
    List<Contract> findByStudentStudentCode(String studentCode);

    boolean existsByStudentIdAndStatus(Long studentId, ContractStatus status);

        @Query("""
            SELECT c FROM Contract c
            JOIN c.room r
            WHERE c.student.id = :studentId
              AND (:status IS NULL OR c.status = :status)
              AND (
                :keyword IS NULL OR :keyword = ''
                OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(c.studentNote, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
        Page<Contract> searchStudentContracts(
            @Param("studentId") Long studentId,
            @Param("status") ContractStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

        @Query("""
            SELECT c FROM Contract c
            JOIN c.room r
            JOIN r.building b
            WHERE c.student.id = :studentId
              AND (
                c.activatedAt IS NOT NULL
                OR c.status = com.dormitory.management.entity.enums.ContractStatus.ACTIVE
                OR c.status = com.dormitory.management.entity.enums.ContractStatus.EXPIRED
              )
              AND (
                :keyword IS NULL OR :keyword = ''
                OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
        Page<Contract> searchResidenceHistoryByStudentId(
            @Param("studentId") Long studentId,
            @Param("keyword") String keyword,
            Pageable pageable);

        @Query("""
            SELECT c FROM Contract c
            JOIN c.student s
            JOIN c.room r
            WHERE (:status IS NULL OR c.status = :status)
              AND (
                :hasStayed IS NULL
                OR (:hasStayed = true AND (c.activatedAt IS NOT NULL OR c.status = com.dormitory.management.entity.enums.ContractStatus.ACTIVE OR c.status = com.dormitory.management.entity.enums.ContractStatus.EXPIRED))
                OR (:hasStayed = false AND c.activatedAt IS NULL AND c.status <> com.dormitory.management.entity.enums.ContractStatus.ACTIVE AND c.status <> com.dormitory.management.entity.enums.ContractStatus.EXPIRED)
              )
              AND (
                :keyword IS NULL OR :keyword = ''
                OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(c.studentNote, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
        Page<Contract> searchContractsForAdmin(
            @Param("status") ContractStatus status,
            @Param("hasStayed") Boolean hasStayed,
            @Param("keyword") String keyword,
            Pageable pageable);
}