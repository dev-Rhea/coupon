package com.gov.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyPaymentSummary {
    LocalDate getPaymentDate();
    Long getTransactionCount();
    BigDecimal getTotalAmount();

}
