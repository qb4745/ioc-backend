package com.cambiaso.ioc.persistence.repository;

import java.time.OffsetDateTime;

public interface EtlJobRepositoryCustom {
    int markStuckAsFailed(OffsetDateTime cutoff);
}
