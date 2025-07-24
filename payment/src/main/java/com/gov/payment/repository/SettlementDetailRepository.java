package com.gov.payment.repository;

import com.gov.payment.entity.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, String> {
}
