package com.analyticalrx.dataplatform.scheduler.entity.schema;


import com.analyticalrx.dataplatform.scheduler.constants.enums.PhiSuppressionStatus;
import com.analyticalrx.dataplatform.scheduler.constants.enums.WebhookEventStatus;
import com.analyticalrx.dataplatform.scheduler.entity.audit.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "webhook_event_audit",
    schema = "ops",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "webhook_event_audit_tenant_id_source_system_idempotency_key_key",
            columnNames = {
                "tenant_id",
                "source_system",
                "idempotency_key"
            }
        )
    }
)
public class WebhookEventAuditEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private PharmacyEntity pharmacy;

    @Column(name = "source_system", nullable = false, columnDefinition = "text")
    private String sourceSystem;

    @Column(name = "source_event_id", columnDefinition = "text")
    private String sourceEventId;

    @Column(name = "source_event_type", nullable = false, columnDefinition = "text")
    private String sourceEventType;

    @Column(name = "receiver_request_id", columnDefinition = "text")
    private String receiverRequestId;

    @Column(name = "idempotency_key", nullable = false, columnDefinition = "text")
    private String idempotencyKey;

    @Column(name = "source_reference_id")
    private UUID sourceReferenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_payload_archive_id")
    private RawPayloadArchiveEntity rawPayloadArchive;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "queued_at")
    private Instant queuedAt;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "text")
    private WebhookEventStatus status = WebhookEventStatus.received;

    @Enumerated(EnumType.STRING)
    @Column(name = "phi_suppression_status", nullable = false, columnDefinition = "text")
    private PhiSuppressionStatus phiSuppressionStatus = PhiSuppressionStatus.suppressed;

    @Column(name = "duplicate_of")
    private UUID duplicateOf;

    @Column(name = "failure_code", columnDefinition = "text")
    private String failureCode;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Column(name = "schema_version", columnDefinition = "text")
    private String schemaVersion;

    @PrePersist
    protected void initializeDefaults() {
        if (receivedAt == null) {
            receivedAt = Instant.now();
        }
    }
}