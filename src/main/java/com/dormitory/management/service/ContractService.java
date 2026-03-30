package com.dormitory.management.service;

import com.dormitory.management.dto.contract.ContractRequestDTO;
import com.dormitory.management.dto.contract.ContractReservationRequestDTO;
import com.dormitory.management.dto.contract.ContractResponseDTO;
import com.dormitory.management.dto.contract.ContractSubmitRequestDTO;
import java.util.List;

public interface ContractService {
    ContractResponseDTO createContract(ContractRequestDTO request);

    ContractResponseDTO reserveBedForCurrentStudent(String username, ContractReservationRequestDTO request);

    ContractResponseDTO submitContractProfile(String username, Long contractId, ContractSubmitRequestDTO request);

    ContractResponseDTO getMyPendingContract(String username);

    ContractResponseDTO approvePendingContract(Long contractId);

    ContractResponseDTO rejectPendingContract(Long contractId, String reason);

    int cancelExpiredPendingContracts();

    List<ContractResponseDTO> getPendingContractsForAdmin();
}