package com.dormitory.management.service;

import com.dormitory.management.dto.contract.ContractRequestDTO;
import com.dormitory.management.dto.contract.ContractReservationRequestDTO;
import com.dormitory.management.dto.contract.ContractResponseDTO;
import com.dormitory.management.dto.contract.ContractSubmitRequestDTO;
import com.dormitory.management.dto.contract.ContractChangeRequestCreateDTO;
import com.dormitory.management.dto.contract.ContractChangeRequestResponseDTO;
import com.dormitory.management.dto.common.PagedResponseDTO;
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

        PagedResponseDTO<ContractResponseDTO> getMyContracts(
            String username,
            String status,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction);

        ContractResponseDTO getMyContractDetail(String username, Long contractId);

        PagedResponseDTO<ContractResponseDTO> getContractsForAdmin(
            String status,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction);

        ContractResponseDTO getContractDetailForAdmin(Long contractId);

        ContractResponseDTO cancelContractEarlyByAdmin(Long contractId, String reason);

            PagedResponseDTO<ContractChangeRequestResponseDTO> getMyContractChangeRequests(
                String username,
                String status,
                String changeType,
                String keyword,
                int page,
                int size,
                String sortBy,
                String direction);

            ContractChangeRequestResponseDTO createMyContractChangeRequest(
                String username,
                Long contractId,
                ContractChangeRequestCreateDTO request);

            PagedResponseDTO<ContractChangeRequestResponseDTO> getContractChangeRequestsForAdmin(
                String status,
                String changeType,
                String keyword,
                int page,
                int size,
                String sortBy,
                String direction);

            ContractChangeRequestResponseDTO approveContractChangeRequest(Long requestId, String adminUsername, String adminNote);

            ContractChangeRequestResponseDTO rejectContractChangeRequest(Long requestId, String adminUsername, String adminNote);

            int expireActiveContractsAndReleaseBeds();
}