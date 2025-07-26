package com.gov.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySettlementSummary {
    LocalDate getSettlementDate();
    Long getSettlementCount();
    BigDecimal getTotalAmount();
    BigDecimal getNetAmount();

}
