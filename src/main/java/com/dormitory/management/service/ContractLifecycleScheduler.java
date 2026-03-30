package com.dormitory.management.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ContractLifecycleScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractLifecycleScheduler.class);

    private final ContractService contractService;

    public ContractLifecycleScheduler(ContractService contractService) {
        this.contractService = contractService;
    }

    @Scheduled(fixedDelay = 60000)
    public void cancelExpiredPendingContracts() {
        int cancelled = contractService.cancelExpiredPendingContracts();
        if (cancelled > 0) {
            LOGGER.info("Auto-cancelled {} expired pending contracts", cancelled);
        }
    }
}
