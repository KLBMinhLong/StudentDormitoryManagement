package com.dormitory.management.service.impl;

import com.dormitory.management.dto.contract.ContractRequestDTO;
import com.dormitory.management.dto.contract.ContractReservationRequestDTO;
import com.dormitory.management.dto.contract.ContractResponseDTO;
import com.dormitory.management.dto.contract.ContractSubmitRequestDTO;
import com.dormitory.management.dto.contract.ContractChangeRequestCreateDTO;
import com.dormitory.management.dto.contract.ContractChangeRequestResponseDTO;
import com.dormitory.management.dto.common.PagedResponseDTO;
import com.dormitory.management.entity.AppUser;
import com.dormitory.management.entity.Bed;
import com.dormitory.management.entity.Building;
import com.dormitory.management.entity.Contract;
import com.dormitory.management.entity.ContractChangeRequest;
import com.dormitory.management.entity.Room;
import com.dormitory.management.entity.RoomType;
import com.dormitory.management.entity.Student;
import com.dormitory.management.entity.enums.ContractChangeRequestStatus;
import com.dormitory.management.entity.enums.ContractChangeType;
import com.dormitory.management.entity.enums.ContractStatus;
import com.dormitory.management.entity.enums.RoomStatus;
import com.dormitory.management.exception.AppException;
import com.dormitory.management.exception.ErrorCode;
import com.dormitory.management.repository.AppUserRepository;
import com.dormitory.management.repository.BedRepository;
import com.dormitory.management.repository.ContractChangeRequestRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private static final int RESERVE_MINUTES = 10;
    private static final int APPROVAL_HOURS = 48;
    private static final int DEFAULT_DURATION_MONTHS = 6;

    private final ContractRepository contractRepository;
    private final ContractChangeRequestRepository contractChangeRequestRepository;
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

        int durationMonths = resolveDurationMonths(request.getDurationMonths());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(durationMonths).minusDays(1);
        BigDecimal monthlyPrice = normalizeMoney(room.getRoomType().getBasePrice());
        BigDecimal depositAmount = monthlyPrice;
        BigDecimal totalAmount = monthlyPrice.multiply(BigDecimal.valueOf(durationMonths));

        Contract contract = Contract.builder()
                .startDate(startDate)
                .endDate(endDate)
                .durationMonths(durationMonths)
                .depositAmount(depositAmount)
                .monthlyRoomPrice(monthlyPrice)
                .totalRoomAmount(totalAmount)
                .status(ContractStatus.ACTIVE)
                .holdExpiresAt(LocalDateTime.now())
                .submitted(true)
                .submittedAt(LocalDateTime.now())
                .activatedAt(LocalDateTime.now())
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

        int durationMonths = resolveDurationMonths(request.getDurationMonths());
        LocalDate provisionalStartDate = LocalDate.now();
        BigDecimal monthlyPrice = normalizeMoney(contract.getRoom().getRoomType().getBasePrice());
        BigDecimal totalAmount = monthlyPrice.multiply(BigDecimal.valueOf(durationMonths));

        contract.setStartDate(provisionalStartDate);
        contract.setDurationMonths(durationMonths);
        contract.setEndDate(provisionalStartDate.plusMonths(durationMonths).minusDays(1));
        contract.setDepositAmount(monthlyPrice);
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

        LocalDateTime approvedAt = LocalDateTime.now();
        int durationMonths = resolveDurationMonths(contract.getDurationMonths());
        LocalDate approvedStartDate = approvedAt.toLocalDate();
        BigDecimal monthlyPrice = normalizeMoney(contract.getMonthlyRoomPrice() == null
                ? contract.getRoom().getRoomType().getBasePrice()
                : contract.getMonthlyRoomPrice());

        contract.setStatus(ContractStatus.ACTIVE);
        contract.setStartDate(approvedStartDate);
        contract.setDurationMonths(durationMonths);
        contract.setEndDate(approvedStartDate.plusMonths(durationMonths).minusDays(1));
        contract.setDepositAmount(monthlyPrice);
        contract.setMonthlyRoomPrice(monthlyPrice);
        contract.setTotalRoomAmount(monthlyPrice.multiply(BigDecimal.valueOf(durationMonths)));
        contract.setHoldExpiresAt(approvedAt);
        if (contract.getActivatedAt() == null) {
            contract.setActivatedAt(approvedAt);
        }
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
    @Transactional
    public int expireActiveContractsAndReleaseBeds() {
        List<Contract> expiredContracts = contractRepository.findByStatusAndEndDateBefore(ContractStatus.ACTIVE, LocalDate.now());
        for (Contract contract : expiredContracts) {
            Bed bed = bedRepository.findByIdForUpdate(contract.getBed().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy giường"));

            contract.setStatus(ContractStatus.EXPIRED);
            contract.setStudentNote("Hợp đồng tự động hết hạn, sinh viên hoàn tất trả phòng tại ban quản lý");
            contractRepository.save(contract);

            if (bed.getStudent() == null || Objects.equals(bed.getStudent().getId(), contract.getStudent().getId())) {
                bed.setOccupied(false);
                bed.setStudent(null);
                bed.setReservedUntil(null);
                bed.setReservedContractId(null);
                bedRepository.save(bed);
                updateRoomStatusByBeds(contract.getRoom());
            }
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

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ContractResponseDTO> getMyContracts(
            String username,
            String status,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {
        Student student = resolveStudentByUsername(username);
        ContractStatus parsedStatus = parseStatus(status);
        Pageable pageable = buildContractPageable(page, size, sortBy, direction);
        Page<ContractResponseDTO> dtoPage = contractRepository
            .searchStudentContracts(student.getId(), parsedStatus, normalizeKeyword(keyword), pageable)
            .map(this::toResponse);
        return PagedResponseDTO.fromPage(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponseDTO getMyContractDetail(String username, Long contractId) {
        Student student = resolveStudentByUsername(username);
        Contract contract = contractRepository.findByIdAndStudentId(contractId, student.getId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy hợp đồng"));
        return toResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ContractResponseDTO> getContractsForAdmin(
            String status,
            String keyword,
            String occupancyType,
            int page,
            int size,
            String sortBy,
            String direction) {
        ContractStatus parsedStatus = parseStatus(status);
        Boolean hasStayed = parseHasStayed(occupancyType);
        Pageable pageable = buildContractPageable(page, size, sortBy, direction);
        Page<ContractResponseDTO> dtoPage = contractRepository
            .searchContractsForAdmin(parsedStatus, hasStayed, normalizeKeyword(keyword), pageable)
            .map(this::toResponse);
        return PagedResponseDTO.fromPage(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponseDTO getContractDetailForAdmin(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy hợp đồng"));
        return toResponse(contract);
    }

    @Override
    @Transactional
    public ContractResponseDTO cancelContractEarlyByAdmin(Long contractId, String reason) {
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy hợp đồng"));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ có thể hủy sớm hợp đồng đang hiệu lực");
        }

        Bed bed = bedRepository.findByIdForUpdate(contract.getBed().getId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy giường"));

        String normalizedReason = (reason == null || reason.isBlank())
            ? "Hủy hợp đồng sớm bởi quản trị viên"
            : reason.trim();

        contract.setStatus(ContractStatus.CANCELLED);
        contract.setStudentNote(normalizedReason);
        contract.setHoldExpiresAt(LocalDateTime.now());

        Contract saved = contractRepository.save(contract);

        if (bed.getStudent() == null || Objects.equals(bed.getStudent().getId(), contract.getStudent().getId())) {
            bed.setOccupied(false);
            bed.setStudent(null);
            bed.setReservedUntil(null);
            bed.setReservedContractId(null);
            bedRepository.save(bed);
            updateRoomStatusByBeds(contract.getRoom());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ContractChangeRequestResponseDTO> getMyContractChangeRequests(
            String username,
            String status,
            String changeType,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {
        Student student = resolveStudentByUsername(username);
        ContractChangeRequestStatus parsedStatus = parseRequestStatus(status);
        ContractChangeType parsedType = parseChangeType(changeType);
        Pageable pageable = buildChangeRequestPageable(page, size, sortBy, direction);

        Page<ContractChangeRequestResponseDTO> dtoPage = contractChangeRequestRepository
                .searchByStudent(student.getId(), parsedStatus, parsedType, normalizeKeyword(keyword), pageable)
                .map(this::toChangeRequestResponse);
        return PagedResponseDTO.fromPage(dtoPage);
    }

    @Override
    @Transactional
    public ContractChangeRequestResponseDTO createMyContractChangeRequest(
            String username,
            Long contractId,
            ContractChangeRequestCreateDTO request) {
        Student student = resolveStudentByUsername(username);
        Contract contract = contractRepository.findByIdAndStudentId(contractId, student.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy hợp đồng"));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ hợp đồng đang hiệu lực mới được gửi yêu cầu thay đổi");
        }

        if (request.getChangeType() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Loại yêu cầu là bắt buộc");
        }

        if (contractChangeRequestRepository.existsByContractIdAndStatus(contract.getId(), ContractChangeRequestStatus.PENDING)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Hợp đồng này đang có yêu cầu chờ xử lý");
        }

        if (request.getChangeType() == ContractChangeType.EXTEND) {
            if (request.getRequestedEndDate() == null) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Yêu cầu gia hạn phải có ngày kết thúc mới");
            }
            int extensionMonths = resolveExtensionMonths(contract.getEndDate(), request.getRequestedEndDate());
            request.setRequestedEndDate(contract.getEndDate().plusMonths(extensionMonths));
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Vui lòng nhập lý do yêu cầu thay đổi hợp đồng");
        }

        ContractChangeRequest saved = contractChangeRequestRepository.save(ContractChangeRequest.builder()
                .changeType(request.getChangeType())
                .status(ContractChangeRequestStatus.PENDING)
                .requestedEndDate(request.getChangeType() == ContractChangeType.EXTEND ? request.getRequestedEndDate() : null)
                .reason(request.getReason().trim())
                .contract(contract)
                .student(student)
                .build());

        return toChangeRequestResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ContractChangeRequestResponseDTO> getContractChangeRequestsForAdmin(
            String status,
            String changeType,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {
        ContractChangeRequestStatus parsedStatus = parseRequestStatus(status);
        ContractChangeType parsedType = parseChangeType(changeType);
        Pageable pageable = buildChangeRequestPageable(page, size, sortBy, direction);

        Page<ContractChangeRequestResponseDTO> dtoPage = contractChangeRequestRepository
                .searchForAdmin(parsedStatus, parsedType, normalizeKeyword(keyword), pageable)
                .map(this::toChangeRequestResponse);
        return PagedResponseDTO.fromPage(dtoPage);
    }

    @Override
    @Transactional
    public ContractChangeRequestResponseDTO approveContractChangeRequest(Long requestId, String adminUsername, String adminNote) {
        ContractChangeRequest request = contractChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy yêu cầu thay đổi hợp đồng"));

        if (request.getStatus() != ContractChangeRequestStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Yêu cầu này đã được xử lý trước đó");
        }

        Contract contract = contractRepository.findById(request.getContract().getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy hợp đồng"));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ duyệt yêu cầu cho hợp đồng đang hiệu lực");
        }

        if (request.getChangeType() == ContractChangeType.CANCEL) {
            Bed bed = bedRepository.findByIdForUpdate(contract.getBed().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy giường"));
            releaseBedAndCancelContract(contract, bed, "Hủy theo yêu cầu sinh viên sau khi đối soát tại ban quản lý");
        } else {
            LocalDate requestedEndDate = request.getRequestedEndDate();
            int extensionMonths = resolveExtensionMonths(contract.getEndDate(), requestedEndDate);
            LocalDate normalizedEndDate = contract.getEndDate().plusMonths(extensionMonths);

            BigDecimal monthlyPrice = normalizeMoney(contract.getMonthlyRoomPrice() == null
                    ? contract.getRoom().getRoomType().getBasePrice()
                    : contract.getMonthlyRoomPrice());
            int durationMonths = Math.max(1,
                (normalizedEndDate.getYear() - contract.getStartDate().getYear()) * 12
                    + normalizedEndDate.getMonthValue() - contract.getStartDate().getMonthValue() + 1);

            contract.setEndDate(normalizedEndDate);
            contract.setDurationMonths(durationMonths);
            contract.setMonthlyRoomPrice(monthlyPrice);
            contract.setTotalRoomAmount(monthlyPrice.multiply(BigDecimal.valueOf(durationMonths)));
            contractRepository.save(contract);
        }

        request.setStatus(ContractChangeRequestStatus.APPROVED);
        request.setAdminNote(adminNote == null ? null : adminNote.trim());
        request.setResolvedAt(LocalDateTime.now());
        request.setResolvedBy(adminUsername);
        return toChangeRequestResponse(contractChangeRequestRepository.save(request));
    }

    @Override
    @Transactional
    public ContractChangeRequestResponseDTO rejectContractChangeRequest(Long requestId, String adminUsername, String adminNote) {
        ContractChangeRequest request = contractChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy yêu cầu thay đổi hợp đồng"));

        if (request.getStatus() != ContractChangeRequestStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Yêu cầu này đã được xử lý trước đó");
        }

        request.setStatus(ContractChangeRequestStatus.REJECTED);
        request.setAdminNote(adminNote == null ? null : adminNote.trim());
        request.setResolvedAt(LocalDateTime.now());
        request.setResolvedBy(adminUsername);

        return toChangeRequestResponse(contractChangeRequestRepository.save(request));
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
        resolveDurationMonths(request.getDurationMonths());
    }

    private int resolveDurationMonths(Integer durationMonths) {
        if (durationMonths == null || (durationMonths != 6 && durationMonths != 12)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Thời hạn hợp đồng chỉ hỗ trợ 6 hoặc 12 tháng");
        }
        return durationMonths;
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

    private Pageable buildContractPageable(int page, int size, String sortBy, String direction) {
        String normalizedSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        Set<String> allowedSorts = Set.of("id", "createdAt", "updatedAt", "startDate", "endDate", "holdExpiresAt", "status");
        if (!allowedSorts.contains(normalizedSortBy)) {
            normalizedSortBy = "createdAt";
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(sortDirection, normalizedSortBy));
    }

    private ContractStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return ContractStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Trạng thái hợp đồng không hợp lệ");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private Boolean parseHasStayed(String occupancyType) {
        if (occupancyType == null || occupancyType.isBlank()) {
            return null;
        }

        String normalized = occupancyType.trim().toUpperCase();
        if ("STAYED".equals(normalized)) {
            return Boolean.TRUE;
        }
        if ("NOT_STAYED".equals(normalized)) {
            return Boolean.FALSE;
        }

        throw new AppException(ErrorCode.BAD_REQUEST, "Bộ lọc thực tế ở không hợp lệ");
    }

    private boolean hasEverActivated(Contract contract) {
        if (contract.getActivatedAt() != null) {
            return true;
        }

        ContractStatus status = contract.getStatus();
        return status == ContractStatus.ACTIVE || status == ContractStatus.EXPIRED;
    }

    private Pageable buildChangeRequestPageable(int page, int size, String sortBy, String direction) {
        String normalizedSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        Set<String> allowedSorts = Set.of("id", "createdAt", "updatedAt", "resolvedAt", "status", "changeType");
        if (!allowedSorts.contains(normalizedSortBy)) {
            normalizedSortBy = "createdAt";
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(sortDirection, normalizedSortBy));
    }

    private ContractChangeRequestStatus parseRequestStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return ContractChangeRequestStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Trạng thái yêu cầu không hợp lệ");
        }
    }

    private ContractChangeType parseChangeType(String changeType) {
        if (changeType == null || changeType.isBlank()) {
            return null;
        }

        try {
            return ContractChangeType.valueOf(changeType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Loại yêu cầu không hợp lệ");
        }
    }

    private void releaseBedAndCancelContract(Contract contract, Bed bed, String reason) {
        contract.setStatus(ContractStatus.CANCELLED);
        contract.setStudentNote(reason);
        contract.setHoldExpiresAt(LocalDateTime.now());
        contractRepository.save(contract);

        if (bed.getStudent() == null || Objects.equals(bed.getStudent().getId(), contract.getStudent().getId())) {
            bed.setOccupied(false);
            bed.setStudent(null);
            bed.setReservedUntil(null);
            bed.setReservedContractId(null);
            bedRepository.save(bed);
            updateRoomStatusByBeds(contract.getRoom());
        }
    }

    private ContractChangeRequestResponseDTO toChangeRequestResponse(ContractChangeRequest request) {
        Contract contract = request.getContract();
        Student student = request.getStudent();
        Integer extensionMonths = null;
        BigDecimal additionalAmount = null;

        if (request.getChangeType() == ContractChangeType.EXTEND && contract != null && request.getRequestedEndDate() != null) {
            extensionMonths = tryResolveExtensionMonths(contract.getEndDate(), request.getRequestedEndDate());
            BigDecimal monthlyPrice = normalizeMoney(contract.getMonthlyRoomPrice() == null
                    ? contract.getRoom().getRoomType().getBasePrice()
                    : contract.getMonthlyRoomPrice());
            if (extensionMonths != null) {
                additionalAmount = monthlyPrice.multiply(BigDecimal.valueOf(extensionMonths));
            }
        }

        return ContractChangeRequestResponseDTO.builder()
                .id(request.getId())
                .contractId(contract == null ? null : contract.getId())
                .studentCode(student == null ? null : student.getStudentCode())
                .studentName(student == null ? null : student.getFullName())
                .roomNumber(contract == null || contract.getRoom() == null ? null : contract.getRoom().getRoomNumber())
                .bedNumber(contract == null || contract.getBed() == null ? 0 : contract.getBed().getBedNumber())
                .changeType(request.getChangeType())
                .status(request.getStatus())
                .currentEndDate(contract == null ? null : contract.getEndDate())
                .requestedEndDate(request.getRequestedEndDate())
                .extensionMonths(extensionMonths)
                .additionalAmount(additionalAmount)
                .reason(request.getReason())
                .adminNote(request.getAdminNote())
                .resolvedAt(request.getResolvedAt())
                .resolvedBy(request.getResolvedBy())
                .createdAt(request.getCreatedAt())
                .build();
    }

    private int resolveExtensionMonths(LocalDate currentEndDate, LocalDate requestedEndDate) {
        if (currentEndDate == null || requestedEndDate == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Ngày gia hạn không hợp lệ");
        }

        if (!requestedEndDate.isAfter(currentEndDate)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Ngày gia hạn phải lớn hơn ngày kết thúc hiện tại");
        }

        if (requestedEndDate.equals(currentEndDate.plusMonths(6))) {
            return 6;
        }

        if (requestedEndDate.equals(currentEndDate.plusMonths(12))) {
            return 12;
        }

        int monthDiff = (requestedEndDate.getYear() - currentEndDate.getYear()) * 12
                + requestedEndDate.getMonthValue() - currentEndDate.getMonthValue();
        if (monthDiff == 6 || monthDiff == 12) {
            return monthDiff;
        }

        throw new AppException(ErrorCode.BAD_REQUEST, "Gia hạn chỉ hỗ trợ 6 hoặc 12 tháng");
    }

    private Integer tryResolveExtensionMonths(LocalDate currentEndDate, LocalDate requestedEndDate) {
        try {
            return resolveExtensionMonths(currentEndDate, requestedEndDate);
        } catch (AppException ex) {
            return null;
        }
    }

    private ContractResponseDTO toResponse(Contract contract) {
        return ContractResponseDTO.builder()
                .id(contract.getId())
                .studentId(contract.getStudent() == null ? null : contract.getStudent().getId())
            .studentCode(contract.getStudent() == null ? null : contract.getStudent().getStudentCode())
                .studentName(contract.getStudent() == null ? null : contract.getStudent().getFullName())
                .studentPhone(contract.getStudent() == null ? null : contract.getStudent().getPhone())
                .studentEmail(contract.getStudent() == null ? null : contract.getStudent().getEmail())
                .studentCccd(contract.getStudent() == null ? null : contract.getStudent().getCccd())
                .roomId(contract.getRoom() == null ? null : contract.getRoom().getId())
                .buildingName(contract.getRoom() == null || contract.getRoom().getBuilding() == null
                        ? null
                        : contract.getRoom().getBuilding().getName())
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
                .activatedAt(contract.getActivatedAt())
                .everActivated(hasEverActivated(contract))
                .emergencyContactName(contract.getEmergencyContactName())
                .emergencyContactPhone(contract.getEmergencyContactPhone())
                .guardianName(contract.getGuardianName())
                .guardianPhone(contract.getGuardianPhone())
                .studentNote(contract.getStudentNote())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}