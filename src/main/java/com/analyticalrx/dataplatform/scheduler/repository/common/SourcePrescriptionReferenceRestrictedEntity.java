package com.analyticalrx.dataplatform.scheduler.repository.common;

import com.analyticalrx.dataplatform.scheduler.entity.audit.DrugAndPayerAuditBaseEntity;
import com.analyticalrx.dataplatform.scheduler.entity.schema.PharmacyEntity;
import com.analyticalrx.dataplatform.scheduler.entity.schema.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "source_prescription_reference_restricted",
    schema = "ops",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "source_prescription_reference_restricted_tenant_id_fill_reference_token_key",
            columnNames = {
                "tenant_id",
                "fill_reference_token"
            }
        )
    }
)
public class SourcePrescriptionReferenceRestrictedEntity extends DrugAndPayerAuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private PharmacyEntity pharmacy;

    @Column(name = "source_system", nullable = false, columnDefinition = "text")
    private String sourceSystem;

    @Column(name = "source_pharmacy_id", columnDefinition = "text")
    private String sourcePharmacyId;

    @Column(name = "rx_no", nullable = false, columnDefinition = "text")
    private String rxNo;

    @Column(name = "refill_no", columnDefinition = "text")
    private String refillNo;

    @Column(name = "prescription_reference_token", nullable = false, columnDefinition = "text")
    private String prescriptionReferenceToken;

    @Column(name = "fill_reference_token", columnDefinition = "text")
    private String fillReferenceToken;
}