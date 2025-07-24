package com.gov.settlement.controller;

import com.gov.settlement.dto.SettlementDto;
import com.gov.settlement.service.SettlementService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {

    private SettlementService settlementService;

    /**
     * 일일 정산 실행 (관리자용)
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> processDailySettlement(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            settlementService.processDailySettlement(date);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "정산 처리가 완료되었습니다.",
                "date", date.toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 정산 승인
     */
    @PutMapping("/{settlementId}/approve")
    public ResponseEntity<Map<String, String>> approveSettlement(@PathVariable String settlementId) {
        try {
            settlementService.approveSettlement(settlementId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "정산이 승인되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 정산 완료
     */
    @PutMapping("/{settlementId}/complete")
    public ResponseEntity<Map<String, String>> completeSettlement(@PathVariable String settlementId) {
        try {
            settlementService.completeSettlement(settlementId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "정산이 완료되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 가맹점별 정산 내역 조회
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<SettlementDto>> getSettlementsByMerchant(@PathVariable String merchantId) {
        List<SettlementDto> settlements = settlementService.getSettlementsByMerchant(merchantId);
        return ResponseEntity.ok(settlements);
    }

    /**
     * 특정 날짜 정산 내역 조회
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<SettlementDto>> getSettlementsByDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SettlementDto> settlements = settlementService.getSettlementsByDate(date);
        return ResponseEntity.ok(settlements);
    }

    /**
     * 대기 중인 정산 내역 조회
     */
    @GetMapping("/pending")
    public ResponseEntity<List<SettlementDto>> getPendingSettlements() {
        List<SettlementDto> settlements = settlementService.getPendingSettlements();
        return ResponseEntity.ok(settlements);
    }

}
