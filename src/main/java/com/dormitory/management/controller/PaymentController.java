package com.dormitory.management.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.service.InvoiceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentController.class);

    private final InvoiceService invoiceService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/webhook/payos", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Void> handlePayOsWebhook(
            @RequestBody(required = false) String payloadRaw,
            @RequestHeader(value = "x-payos-signature", required = false) String signature) {
        JsonNode payload;
        try {
            if (payloadRaw == null || payloadRaw.isBlank()) {
                payload = objectMapper.createObjectNode();
            } else {
                payload = objectMapper.readTree(payloadRaw);
            }
            invoiceService.handlePayOsWebhook(payload, signature);
        } catch (Exception ex) {
            // Always ACK 200 for webhook verification/ping, even when payload is invalid.
            LOGGER.warn("PayOS webhook processing failed but acknowledged. payload={}", payloadRaw, ex);
        }
        return ResponseEntity.ok().build();
    }
}
