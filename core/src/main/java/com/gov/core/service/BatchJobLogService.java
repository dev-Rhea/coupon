package com.gov.core.service;

import com.gov.core.entity.BatchJobLog;
import com.gov.core.entity.BatchJobLog.BatchJobStatus;
import com.gov.core.entity.BatchJobLog.BatchJobType;
import com.gov.core.repository.BatchJobLogRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class BatchJobLogService {

    private final BatchJobLogRepository batchJobLogRepository;

    public BatchJobLogService(BatchJobLogRepository batchJobLogRepository) {
        this.batchJobLogRepository = batchJobLogRepository;
    }

    /**
     * 배치 작업 시작 로그
     */
    public BatchJobLog startJob(String jobType, String jobName, Map<String, Object> parameters) {
        BatchJobLog jobLog = BatchJobLog.builder()
            .logId(UUID.randomUUID().toString())
            .jobName(jobName)
            .jobType(BatchJobType.valueOf(jobType))
            .status(BatchJobStatus.RUNNING)
            .startTime(LocalDateTime.now())
            .parameters(parameters)
            .build();

        return batchJobLogRepository.save(jobLog);
    }

    /**
     * 배치 작업 완료 로그
     */
    public void completeJob(String logId, int totalCount, int successCount, int errorCount) {
        BatchJobLog jobLog = batchJobLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("배치 작업 로그를 찾을 수 없습니다: " + logId));

        jobLog.complete(totalCount, successCount, errorCount);
        batchJobLogRepository.save(jobLog);
    }

    /**
     * 배치 작업 실패 로그
     */
    public void failJob(String logId, String errorMessage) {
        BatchJobLog jobLog = batchJobLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("배치 작업 로그를 찾을 수 없습니다: " + logId));

        jobLog.fail(errorMessage);
        batchJobLogRepository.save(jobLog);
    }
}