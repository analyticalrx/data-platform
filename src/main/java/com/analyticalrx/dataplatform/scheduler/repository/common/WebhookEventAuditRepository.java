package com.analyticalrx.dataplatform.scheduler.repository.common;


import com.analyticalrx.dataplatform.scheduler.constants.enums.WebhookEventStatus;
import com.analyticalrx.dataplatform.scheduler.entity.schema.WebhookEventAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookEventAuditRepository extends JpaRepository<WebhookEventAuditEntity, UUID> {

    Optional<WebhookEventAuditEntity> findByTenant_IdAndSourceSystemAndIdempotencyKey(UUID tenantId, String sourceSystem, String idempotencyKey);

    boolean existsByTenant_IdAndSourceSystemAndIdempotencyKey(UUID tenantId, String sourceSystem, String idempotencyKey);

    List<WebhookEventAuditEntity> findByTenant_IdAndStatus(UUID tenantId, WebhookEventStatus status);

    List<WebhookEventAuditEntity> findByStatus(WebhookEventStatus status);

    List<WebhookEventAuditEntity> findByDuplicateOf(UUID duplicateOf);
}