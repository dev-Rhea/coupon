package com.gov.payment.repository;

import com.gov.payment.entity.Settlement;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, String> {

    Optional<Settlement> findByMerchantIdAndSettlementDate(String merchantId, LocalDate settlementDate);

}
