package com.analyticalrx.dataplatform.scheduler.entity.schema;

import com.analyticalrx.dataplatform.scheduler.constants.enums.TenantStatus;
import com.analyticalrx.dataplatform.scheduler.entity.audit.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pharmacy")
public class PharmacyEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "pharmacy_tenant_id_fkey"))
    private TenantEntity tenant;

    @Column(name = "pharmacy_key", nullable = false, columnDefinition = "text")
    private String pharmacyKey;

    @Column(name = "source_system", nullable = false, columnDefinition = "text")
    private String sourceSystem;

    @Column(name = "source_pharmacy_id", columnDefinition = "text")
    private String sourcePharmacyId;

    @Column(name = "display_name", nullable = false, columnDefinition = "text")
    private String displayName;

    @Column(name = "npi", columnDefinition = "text")
    private String npi;

    @Column(name = "ncpdp", columnDefinition = "text")
    private String ncpdp;

    @Column(name = "state_code", columnDefinition = "text")
    private String stateCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "text")
    private TenantStatus status = TenantStatus.active;
}