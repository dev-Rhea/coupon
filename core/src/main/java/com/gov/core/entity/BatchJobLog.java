package com.gov.core.entity;

import com.gov.core.config.JpaConverterJson;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "batch_job_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BatchJobLog {

    @Id
    @Column(name = "log_id", length = 50)
    private String logId;

    @Column(name = "job_name", length = 100, nullable = false)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private BatchJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BatchJobStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "processed_count")
    private Integer processedCount = 0;

    @Column(name = "success_count")
    private Integer successCount = 0;

    @Column(name = "error_count")
    private Integer errorCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Convert(converter = JpaConverterJson.class)
    @Column(name = "parameters", columnDefinition = "JSON")
    private Map<String, Object> parameters;

    @Builder
    public BatchJobLog(String logId, String jobName, BatchJobType jobType,
        BatchJobStatus status, LocalDateTime startTime,
        Map<String, Object> parameters) {
        this.logId = logId;
        this.jobName = jobName;
        this.jobType = jobType;
        this.status = status;
        this.startTime = startTime;
        this.parameters = parameters;
    }

    public void complete(int totalCount, int successCount, int errorCount) {
        if(totalCount < 0 || successCount < 0 || errorCount < 0) {
            throw new IllegalArgumentException("카운트 값은 음수일 수 없습니다");
        }
        if(successCount + errorCount != totalCount) {
            throw new IllegalArgumentException("성공 + 실패 건수가 전체 건수를 초과할 수 없습니다");
        }
        this.status = BatchJobStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.processedCount = totalCount;
        this.successCount = successCount;
        this.errorCount = errorCount;
    }

    public void fail(String errorMessage) {
        this.status = BatchJobStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    @Getter
    public enum BatchJobType {
        COUPON_EXPIRY("쿠폰 만료 처리"),
        SETTLEMENT("정산 처리"),
        DATA_CLEANUP("데이터 정리");

        private final String description;

        BatchJobType(String description) {
            this.description = description;
        }
    }

    @Getter
    public enum BatchJobStatus {
        RUNNING("실행중"),
        COMPLETED("완료"),
        FAILED("실패");

        private final String description;

        BatchJobStatus(String description) {
            this.description = description;
        }
    }

}
