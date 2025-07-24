package com.gov.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record PaymentReqDto(
    @NotBlank String userId,
    @NotBlank String merchantId,
    @NotBlank String couponId,
    @NotBlank @DecimalMin(value = "0.01") BigDecimal amount
) {

}
