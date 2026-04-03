package com.dormitory.management.repository;

import com.dormitory.management.entity.ContractChangeRequest;
import com.dormitory.management.entity.enums.ContractChangeRequestStatus;
import com.dormitory.management.entity.enums.ContractChangeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractChangeRequestRepository extends JpaRepository<ContractChangeRequest, Long> {

    boolean existsByContractIdAndStatus(Long contractId, ContractChangeRequestStatus status);

    Optional<ContractChangeRequest> findByIdAndStudentId(Long id, Long studentId);

        @EntityGraph(attributePaths = {"student", "contract", "contract.room", "contract.room.building", "contract.bed"})
    @Query("""
            SELECT r FROM ContractChangeRequest r
            JOIN r.contract c
            WHERE r.student.id = :studentId
              AND (:status IS NULL OR r.status = :status)
              AND (:changeType IS NULL OR r.changeType = :changeType)
              AND (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(COALESCE(r.reason, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.room.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<ContractChangeRequest> searchByStudent(
            @Param("studentId") Long studentId,
            @Param("status") ContractChangeRequestStatus status,
            @Param("changeType") ContractChangeType changeType,
            @Param("keyword") String keyword,
            Pageable pageable);

        @EntityGraph(attributePaths = {"student", "contract", "contract.room", "contract.room.building", "contract.bed"})
    @Query("""
            SELECT r FROM ContractChangeRequest r
            JOIN r.contract c
            JOIN r.student s
            WHERE (:status IS NULL OR r.status = :status)
              AND (:changeType IS NULL OR r.changeType = :changeType)
              AND (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.room.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(r.reason, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<ContractChangeRequest> searchForAdmin(
            @Param("status") ContractChangeRequestStatus status,
            @Param("changeType") ContractChangeType changeType,
            @Param("keyword") String keyword,
            Pageable pageable);
}
