package com.dormitory.management.service.impl;

import com.dormitory.management.dto.contract.ContractRequestDTO;
import com.dormitory.management.dto.contract.ContractReservationRequestDTO;
import com.dormitory.management.dto.contract.ContractResponseDTO;
import com.dormitory.management.dto.contract.ContractSubmitRequestDTO;
import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Bed;
import com.dormitory.management.entity.Building;
import com.dormitory.management.entity.Contract;
import com.dormitory.management.entity.Room;
import com.dormitory.management.entity.RoomType;
import com.dormitory.management.entity.Student;
import com.dormitory.management.entity.enums.ContractStatus;
import com.dormitory.management.entity.enums.RoomStatus;
import com.dormitory.management.exception.AppException;
import com.dormitory.management.exception.ErrorCode;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.BedRepository;
import com.dormitory.management.repository.ContractRepository;
import com.dormitory.management.repository.RoomRepository;
import com.dormitory.management.repository.StudentRepository;
import com.dormitory.management.service.ContractService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private static final int RESERVE_MINUTES = 10;
    private static final int APPROVAL_HOURS = 48;
    private static final int DEFAULT_DURATION_MONTHS = 6;

    private final ContractRepository contractRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public ContractResponseDTO createContract(ContractRequestDTO request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy sinh viên"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy phòng"));
        Bed bed = bedRepository.findByIdForUpdate(request.getBedId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy giường"));

        if (!Objects.equals(bed.getRoom().getId(), room.getId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Giường không thuộc phòng đã chọn");
        }

        validateStudentRoomGender(student, room);
        ensureStudentHasNoOpenContract(student.getId());
        ensureBedCanBeReserved(bed, LocalDateTime.now());

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Ngày bắt đầu/kết thúc hợp lệ là bắt buộc");
        }

        int durationMonths = Math.max(1, (endDate.getYear() - startDate.getYear()) * 12 + endDate.getMonthValue() - startDate.getMonthValue() + 1);
        BigDecimal monthlyPrice = normalizeMoney(room.getRoomType().getBasePrice());
        BigDecimal totalAmount = monthlyPrice.multiply(BigDecimal.valueOf(durationMonths));

        Contract contract = Contract.builder()
                .startDate(startDate)
                .endDate(endDate)
                .durationMonths(durationMonths)
                .depositAmount(normalizeMoney(request.getDepositAmount()))
                .monthlyRoomPrice(monthlyPrice)
                .totalRoomAmount(totalAmount)
                .status(ContractStatus.ACTIVE)
                .holdExpiresAt(LocalDateTime.now())
                .submitted(true)
                .submittedAt(LocalDateTime.now())
                .student(student)
                .room(room)
                .bed(bed)
                .build();

        Contract savedContract = contractRepository.save(contract);

        bed.setOccupied(true);
        bed.setStudent(student);
        bed.setReservedUntil(null);
        bed.setReservedContractId(null);
        bedRepository.save(bed);

        updateRoomStatusByBeds(room);
        return toResponse(savedContract);
    }

    @Override
    @Transactional
    public ContractResponseDTO reserveBedForCurrentStudent(String username, ContractReservationRequestDTO request) {
        Student student = resolveStudentByUsername(username);
        ensureStudentHasNoOpenContract(student.getId());

        Bed bed = bedRepository.findByIdForUpdate(request.getBedId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy giường"));

        Room room = bed.getRoom();
        validateStudentRoomGender(student, room);
        ensureBedCanBeReserved(bed, LocalDateTime.now());

        LocalDate startDate = LocalDate.now();
        int durationMonths = DEFAULT_DURATION_MONTHS;
        LocalDate endDate = startDate.plusMonths(durationMonths).minusDays(1);
        BigDecimal monthlyPrice = normalizeMoney(room.getRoomType().getBasePrice());
        BigDecimal totalAmount = monthlyPrice.multiply(BigDecimal.valueOf(durationMonths));
        LocalDateTime holdExpiresAt = LocalDateTime.now().plusMinutes(RESERVE_MINUTES);

        Contract contract = Contract.builder()
                .startDate(startDate)
                .endDate(endDate)
                .durationMonths(durationMonths)
                .depositAmount(monthlyPrice)
                .monthlyRoomPrice(monthlyPrice)
                .totalRoomAmount(totalAmount)
                .status(ContractStatus.PENDING)
                .holdExpiresAt(holdExpiresAt)
                .submitted(false)
                .student(student)
                .room(room)
                .bed(bed)
                .build();

        Contract savedContract = contractRepository.save(contract);

        bed.setOccupied(true);
        bed.setStudent(null);
        bed.setReservedUntil(holdExpiresAt);
        bed.setReservedContractId(savedContract.getId());
        bedRepository.save(bed);

        updateRoomStatusByBeds(room);
        return toResponse(savedContract);
    }

    @Override
    @Transactional
    public ContractResponseDTO submitContractProfile(String username, Long contractId, ContractSubmitRequestDTO request) {
        Student student = resolveStudentByUsername(username);
        Contract contract = contractRepository.findByIdAndStudentId(contractId, student.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy yêu cầu hợp đồng"));

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Yêu cầu này không còn ở trạng thái chờ duyệt");
        }

        LocalDateTime now = LocalDateTime.now();
        if (contract.getHoldExpiresAt().isBefore(now)) {
            cancelPendingContractInternal(contract, "Quá thời gian giữ chỗ");
            throw new AppException(ErrorCode.BAD_REQUEST, "Yêu cầu giữ chỗ đã hết hạn, vui lòng chọn lại giường");
        }

        validateSubmitPayload(request);

        BigDecimal monthlyPrice = normalizeMoney(contract.getRoom().getRoomType().getBasePrice());
        BigDecimal totalAmount = monthlyPrice.multiply(BigDecimal.valueOf(request.getDurationMonths()));

        contract.setStartDate(request.getStartDate());
        contract.setDurationMonths(request.getDurationMonths());
        contract.setEndDate(request.getStartDate().plusMonths(request.getDurationMonths()).minusDays(1));
        contract.setDepositAmount(normalizeMoney(request.getDepositAmount()));
        contract.setMonthlyRoomPrice(monthlyPrice);
        contract.setTotalRoomAmount(totalAmount);
        contract.setEmergencyContactName(request.getEmergencyContactName().trim());
        contract.setEmergencyContactPhone(request.getEmergencyContactPhone().trim());
        contract.setGuardianName(request.getGuardianName().trim());
        contract.setGuardianPhone(request.getGuardianPhone().trim());
        contract.setStudentNote(request.getStudentNote() == null ? null : request.getStudentNote().trim());
        contract.setSubmitted(true);
        contract.setSubmittedAt(now);
        contract.setHoldExpiresAt(now.plusHours(APPROVAL_HOURS));

        Contract saved = contractRepository.save(contract);

        Bed bed = bedRepository.findByIdForUpdate(contract.getBed().getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy giường"));
        bed.setReservedUntil(saved.getHoldExpiresAt());
        bed.setReservedContractId(saved.getId());
        bedRepository.save(bed);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponseDTO getMyPendingContract(String username) {
        Student student = resolveStudentByUsername(username);
        return contractRepository.findFirstByStudentIdAndStatusOrderByCreatedAtDesc(student.getId(), ContractStatus.PENDING)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public ContractResponseDTO approvePendingContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy hợp đồng"));

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ có thể duyệt hợp đồng đang chờ");
        }
        if (!contract.isSubmitted()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Sinh viên chưa hoàn tất nộp hồ sơ");
        }

        if (contract.getHoldExpiresAt().isBefore(LocalDateTime.now())) {
            cancelPendingContractInternal(contract, "Quá thời gian chờ duyệt");
            throw new AppException(ErrorCode.BAD_REQUEST, "Yêu cầu đã quá hạn chờ duyệt");
        }

        Bed bed = bedRepository.findByIdForUpdate(contract.getBed().getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy giường"));

        if (bed.getStudent() != null && !Objects.equals(bed.getStudent().getId(), contract.getStudent().getId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Giường này đã có sinh viên khác sử dụng");
        }

        if (!Objects.equals(bed.getReservedContractId(), contract.getId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Giường không còn giữ cho hợp đồng này");
        }

        contract.setStatus(ContractStatus.ACTIVE);
        contract.setHoldExpiresAt(LocalDateTime.now());
        Contract saved = contractRepository.save(contract);

        bed.setOccupied(true);
        bed.setStudent(contract.getStudent());
        bed.setReservedUntil(null);
        bed.setReservedContractId(null);
        bedRepository.save(bed);

        updateRoomStatusByBeds(contract.getRoom());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ContractResponseDTO rejectPendingContract(Long contractId, String reason) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy hợp đồng"));

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Hợp đồng này không ở trạng thái chờ duyệt");
        }

        String normalizedReason = (reason == null || reason.isBlank())
                ? "Yêu cầu bị từ chối bởi quản trị viên"
                : reason.trim();
        cancelPendingContractInternal(contract, normalizedReason);
        return toResponse(contract);
    }

    @Override
    @Transactional
    public int cancelExpiredPendingContracts() {
        List<Contract> expiredContracts = contractRepository.findByStatusAndHoldExpiresAtBefore(ContractStatus.PENDING, LocalDateTime.now());
        for (Contract contract : expiredContracts) {
            cancelPendingContractInternal(contract, "Tự động hủy do quá hạn xử lý");
        }
        return expiredContracts.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getPendingContractsForAdmin() {
        return contractRepository.findByStatusOrderByCreatedAtDesc(ContractStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void cancelPendingContractInternal(Contract contract, String reason) {
        Bed bed = bedRepository.findByIdForUpdate(contract.getBed().getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy giường"));

        contract.setStatus(ContractStatus.CANCELLED);
        contract.setStudentNote(reason);
        contractRepository.save(contract);

        if (bed.getStudent() == null && Objects.equals(bed.getReservedContractId(), contract.getId())) {
            bed.setOccupied(false);
            bed.setReservedUntil(null);
            bed.setReservedContractId(null);
            bedRepository.save(bed);
            updateRoomStatusByBeds(contract.getRoom());
        }
    }

    private void validateSubmitPayload(ContractSubmitRequestDTO request) {
        if (request.getDurationMonths() == null || (request.getDurationMonths() != 6 && request.getDurationMonths() != 12)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Thời hạn hợp đồng chỉ hỗ trợ 6 hoặc 12 tháng");
        }

        if (request.getStartDate() == null || request.getStartDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Ngày vào ở phải từ hôm nay trở đi");
        }

        if (normalizeMoney(request.getDepositAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Tiền cọc phải lớn hơn 0");
        }
    }

    private void ensureStudentHasNoOpenContract(Long studentId) {
        boolean hasOpenContract = contractRepository.existsByStudentIdAndStatusIn(
                studentId,
                Set.of(ContractStatus.PENDING, ContractStatus.ACTIVE));
        if (hasOpenContract) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Sinh viên đã có hợp đồng đang chờ xử lý hoặc đang hiệu lực");
        }
    }

    private void ensureBedCanBeReserved(Bed bed, LocalDateTime now) {
        if (!bed.isOccupied()) {
            return;
        }

        if (bed.getStudent() != null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Giường này đã có sinh viên sử dụng");
        }

        if (bed.getReservedUntil() != null && bed.getReservedUntil().isAfter(now)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Giường này đang được sinh viên khác giữ chỗ");
        }

        bed.setOccupied(false);
        bed.setReservedUntil(null);
        bed.setReservedContractId(null);
        bedRepository.save(bed);
    }

    private void validateStudentRoomGender(Student student, Room room) {
        Building building = room.getBuilding();
        RoomType roomType = room.getRoomType();

        if (building != null && building.getGenderAllowed() != null && !"Nam/Nữ".equalsIgnoreCase(building.getGenderAllowed())
                && !building.getGenderAllowed().equalsIgnoreCase(student.getGender())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Tòa nhà này chỉ dành cho sinh viên giới tính: " + building.getGenderAllowed());
        }

        String roomGender = room.getGenderAllowed();
        if (roomGender != null && !"Nam/Nữ".equalsIgnoreCase(roomGender) && !roomGender.equalsIgnoreCase(student.getGender())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Phòng này chỉ dành cho sinh viên giới tính: " + roomGender);
        }

        if (roomType != null && roomType.getGenderAllowed() != null && !"Nam/Nữ".equalsIgnoreCase(roomType.getGenderAllowed())
                && !roomType.getGenderAllowed().equalsIgnoreCase(student.getGender())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Loại phòng này không phù hợp với giới tính của sinh viên.");
        }
    }

    private Student resolveStudentByUsername(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản"));

        if (appUser.getStudent() != null) {
            return appUser.getStudent();
        }

        return studentRepository.findByStudentCodeIgnoreCase(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy hồ sơ sinh viên"));
    }

    private void updateRoomStatusByBeds(Room room) {
        List<Bed> beds = bedRepository.findByRoomIdOrderByBedNumberAsc(room.getId());
        boolean hasAvailable = beds.stream().anyMatch((bed) -> !bed.isOccupied());
        RoomStatus targetStatus = hasAvailable ? RoomStatus.AVAILABLE : RoomStatus.FULL;
        if (room.getStatus() != RoomStatus.MAINTENANCE && room.getStatus() != targetStatus) {
            room.setStatus(targetStatus);
            roomRepository.save(room);
        }
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private ContractResponseDTO toResponse(Contract contract) {
        return ContractResponseDTO.builder()
                .id(contract.getId())
                .studentId(contract.getStudent() == null ? null : contract.getStudent().getId())
                .studentName(contract.getStudent() == null ? null : contract.getStudent().getFullName())
                .roomId(contract.getRoom() == null ? null : contract.getRoom().getId())
                .roomNumber(contract.getRoom() == null ? null : contract.getRoom().getRoomNumber())
                .bedId(contract.getBed() == null ? null : contract.getBed().getId())
                .bedNumber(contract.getBed() == null ? 0 : contract.getBed().getBedNumber())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .durationMonths(contract.getDurationMonths())
                .depositAmount(contract.getDepositAmount())
                .monthlyRoomPrice(contract.getMonthlyRoomPrice())
                .totalRoomAmount(contract.getTotalRoomAmount())
                .status(contract.getStatus() == null ? null : contract.getStatus().name())
                .submitted(contract.isSubmitted())
                .holdExpiresAt(contract.getHoldExpiresAt())
                .submittedAt(contract.getSubmittedAt())
                .emergencyContactName(contract.getEmergencyContactName())
                .emergencyContactPhone(contract.getEmergencyContactPhone())
                .guardianName(contract.getGuardianName())
                .guardianPhone(contract.getGuardianPhone())
                .studentNote(contract.getStudentNote())
                .build();
    }
}