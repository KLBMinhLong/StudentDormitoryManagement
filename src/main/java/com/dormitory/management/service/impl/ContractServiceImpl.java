package com.dormitory.management.service.impl;

import com.dormitory.management.entity.*;
import com.dormitory.management.exception.AppException;
import com.dormitory.management.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dormitory.management.dto.contract.ContractRequestDTO;
import com.dormitory.management.dto.contract.ContractResponseDTO;
import com.dormitory.management.entity.enums.ContractStatus;
import com.dormitory.management.entity.enums.RoomStatus;
import com.dormitory.management.repository.BedRepository;
import com.dormitory.management.repository.ContractRepository;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.repository.StudentRepository;
import com.dormitory.management.service.ContractService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;

    @Override
    @Transactional
    public ContractResponseDTO createContract(ContractRequestDTO request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        boolean hasActiveContract = contractRepository.existsByStudentIdAndStatus(student.getId(), ContractStatus.ACTIVE);
        if (hasActiveContract) {
            throw new RuntimeException("Sinh viên này hiện đang có một hợp đồng nội trú còn hiệu lực.");
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
        Building building = room.getBuilding();
        RoomType roomType = room.getRoomType();

        if (!building.getGenderAllowed().equals("Nam/Nữ") &&
                !building.getGenderAllowed().equalsIgnoreCase(student.getGender())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Tòa nhà này chỉ dành cho sinh viên giới tính: " + building.getGenderAllowed());
        }

        String roomGender = room.getGenderAllowed();
        if (roomGender != null && !roomGender.equalsIgnoreCase("Nam/Nữ") &&
                !roomGender.equalsIgnoreCase(student.getGender())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Phòng này chỉ dành cho sinh viên giới tính: " + roomGender);
        }

        if (!roomType.getGenderAllowed().equals("Nam/Nữ") &&
                !roomType.getGenderAllowed().equalsIgnoreCase(student.getGender())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Loại phòng này không phù hợp với giới tính của sinh viên.");
        }

        Bed bed = bedRepository.findById(request.getBedId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giường"));

        if (bed.isOccupied()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Giường này đã có người sử dụng");
        }

        Contract contract = Contract.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .depositAmount(request.getDepositAmount())
                .status(ContractStatus.ACTIVE)
                .student(student)
                .room(room)
                .bed(bed)
                .build();

        Contract savedContract = contractRepository.save(contract);

        bed.setOccupied(true);
        bed.setStudent(student);
        bedRepository.save(bed);

        boolean hasAvailableBeds = bedRepository.existsByRoomIdAndIsOccupiedFalse(room.getId());
        if (!hasAvailableBeds) {
            room.setStatus(RoomStatus.FULL);
            roomRepository.save(room);
        }

        return ContractResponseDTO.builder()
                .id(savedContract.getId())
                .studentId(student.getId())
                .studentName(student.getFullName())
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .bedId(bed.getId())
                .bedNumber(bed.getBedNumber())
                .startDate(savedContract.getStartDate())
                .endDate(savedContract.getEndDate())
                .depositAmount(savedContract.getDepositAmount())
                .status(savedContract.getStatus().name())
                .build();
    }
}