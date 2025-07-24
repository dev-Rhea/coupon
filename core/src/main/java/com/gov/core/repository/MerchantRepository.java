package com.gov.core.repository;

import com.gov.core.entity.Merchant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerchantRepository extends JpaRepository<Merchant, String> {

    /**
     * 카테고리별 가맹점 조회
     */
    List<Merchant> findByCategory(Merchant.MerchantCategory category);

    /**
     * 가맹점명으로 검색
     */
    @Query("SELECT m FROM Merchant m WHERE m.merchantName LIKE %:name%")
    List<Merchant> findByMerchantNameContaining(@Param("name") String name);

}
