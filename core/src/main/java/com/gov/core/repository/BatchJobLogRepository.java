package com.gov.core.repository;

import com.gov.core.entity.BatchJobLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobLogRepository extends JpaRepository<BatchJobLog, String> {


}
