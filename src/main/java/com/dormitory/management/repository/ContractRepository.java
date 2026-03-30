package com.dormitory.management.repository;

import com.dormitory.management.entity.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dormitory.management.entity.Contract;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByIdAndStudentId(Long id, Long studentId);

    Optional<Contract> findFirstByStudentIdAndStatusOrderByCreatedAtDesc(Long studentId, ContractStatus status);

    boolean existsByStudentIdAndStatusIn(Long studentId, Set<ContractStatus> statuses);

    List<Contract> findByStatusAndHoldExpiresAtBefore(ContractStatus status, LocalDateTime time);

    List<Contract> findByStatusOrderByCreatedAtDesc(ContractStatus status);

    // Lấy danh sách hợp đồng theo phòng
    List<Contract> findByRoomId(Long roomId);

    // Lấy danh sách hợp đồng theo mã sinh viên
    List<Contract> findByStudentStudentCode(String studentCode);

    boolean existsByStudentIdAndStatus(Long studentId, ContractStatus status);
}