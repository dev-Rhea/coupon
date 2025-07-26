package com.gov.payment.dto;

import java.math.BigDecimal;

public interface MonthlySettlementSummary {
    Integer getYear();
    Integer getMonth();
    Long getSettlementCount();
    BigDecimal getTotalAmount();
    BigDecimal getNetAmount();

}
