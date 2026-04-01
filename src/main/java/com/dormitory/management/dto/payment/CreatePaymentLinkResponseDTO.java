package com.dormitory.management.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentLinkResponseDTO {

    private Long invoiceId;
    private String invoiceCode;
    private String paymentOrderCode;
    private String paymentLink;
    private String paymentQrCode;
    private String provider;
}
