package com.analyticalrx.dataplatform.scheduler.entity.schema;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "raw_payload_archive")
public class RawPayloadArchiveEntity {

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

    @Column(name = "source_event_type", columnDefinition = "text")
    private String sourceEventType;

    @Column(name = "raw_payload_reference", nullable = false, columnDefinition = "text")
    private String rawPayloadReference;

    @Column(name = "raw_payload_sha256", nullable = false, columnDefinition = "text")
    private String rawPayloadSha256;

    @Column(name = "payload_size_bytes")
    private Integer payloadSizeBytes;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "retention_until", nullable = false)
    private LocalDate retentionUntil;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void initializeDefaults() {
        if (Objects.isNull(retentionUntil)) {
            retentionUntil = LocalDate.now(java.time.ZoneOffset.UTC).plusDays(30);
        }
    }
}