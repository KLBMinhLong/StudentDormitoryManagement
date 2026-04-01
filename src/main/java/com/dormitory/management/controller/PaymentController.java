package com.dormitory.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.service.InvoiceService;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final InvoiceService invoiceService;

    @PostMapping("/webhook/payos")
    public ResponseEntity<ApiResponse<Void>> handlePayOsWebhook(
            @RequestBody JsonNode payload,
            @RequestHeader(value = "x-payos-signature", required = false) String signature) {
        invoiceService.handlePayOsWebhook(payload, signature);
        return ResponseEntity.ok(ApiResponse.success(200, "Webhook PayOS đã được xử lý", null));
    }
}
