package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.persistence.entity.QuarantinedRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuarantinedRecordRepository extends JpaRepository<QuarantinedRecord, Long> {
}
