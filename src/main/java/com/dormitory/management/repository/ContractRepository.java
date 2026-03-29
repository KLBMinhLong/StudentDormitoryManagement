package com.dormitory.management.repository;

import com.dormitory.management.entity.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dormitory.management.entity.Contract;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // Tìm hợp đồng đang hoạt động của một sinh viên cụ thể
    Optional<Contract> findByStudentIdAndStatus(Long studentId, String status);

    // Lấy danh sách hợp đồng theo phòng
    List<Contract> findByRoomId(Long roomId);

    // Lấy danh sách hợp đồng theo mã sinh viên
    List<Contract> findByStudentStudentCode(String studentCode);

    boolean existsByStudentIdAndStatus(Long studentId, ContractStatus status);
}