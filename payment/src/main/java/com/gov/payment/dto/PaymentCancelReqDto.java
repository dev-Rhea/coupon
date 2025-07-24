package com.gov.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentCancelReqDto(
    @NotBlank
    String cancelReason
) {

}
