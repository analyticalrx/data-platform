package com.analyticalrx.dataplatform.scheduler.entity.schema;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PayerType;
import com.analyticalrx.dataplatform.scheduler.entity.audit.DrugAndPayerAuditBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payer")
public class PayerEntity extends DrugAndPayerAuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "source_system", nullable = false, columnDefinition = "text")
    private String sourceSystem;

    @Column(name = "source_payer_id", columnDefinition = "text")
    private String sourcePayerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payer_type", nullable = false, columnDefinition = "text")
    private PayerType payerType = PayerType.insurance;

    @Column(name = "payer_name", columnDefinition = "text")
    private String payerName;

    @Column(name = "payer_bin", columnDefinition = "text")
    private String payerBin;

    @Column(name = "payer_pcn", columnDefinition = "text")
    private String payerPcn;
}