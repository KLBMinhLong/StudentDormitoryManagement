package com.dormitory.management.service;

import com.dormitory.management.dto.contract.ContractRequestDTO;
import com.dormitory.management.dto.contract.ContractResponseDTO;

public interface ContractService {
    ContractResponseDTO createContract(ContractRequestDTO request);
}