package com.gov.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "merchants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Merchant extends BaseTimeEntity {

    @Id
    @Column(name = "merchant_id", length = 50)
    private String merchantId;

    @Column(name = "merchant_name", nullable = false, length = 200)
    private String merchantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private MerchantCategory category;

    @Builder
    public Merchant(String merchantId, String merchantName, MerchantCategory category) {
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.category = category;
    }

    @Getter
    public enum MerchantCategory {
        COFFEE("커피전문점"),
        FAST_FOOD("패스트푸드"),
        SUPERMARKET("대형마트"),
        RESTAURANT("일반음식점"),
        CONVENIENCE("편의점");

        private final String description;

        MerchantCategory(String description) {
            this.description = description;
        }

    }
}
