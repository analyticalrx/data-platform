package com.analyticalrx.dataplatform.scheduler.entity.schema;

import com.analyticalrx.dataplatform.scheduler.entity.audit.DrugAndPayerAuditBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "drug")
public class DrugEntity extends DrugAndPayerAuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "drug_tenant_id_fkey"))
    private TenantEntity tenant;

    @Column(name = "source_system", nullable = false, columnDefinition = "text")
    private String sourceSystem;

    @Column(name = "source_drug_id", columnDefinition = "text")
    private String sourceDrugId;

    @Column(name = "ndc", columnDefinition = "text")
    private String ndc;

    @Column(name = "drug_name", columnDefinition = "text")
    private String drugName;

    @Column(name = "generic_name", columnDefinition = "text")
    private String genericName;

    @Column(name = "brand_name", columnDefinition = "text")
    private String brandName;

    @Column(name = "is_brand")
    private Boolean isBrand;

    @Column(name = "strength", columnDefinition = "text")
    private String strength;

    @Column(name = "strength_unit", columnDefinition = "text")
    private String strengthUnit;

    @Column(name = "form", columnDefinition = "text")
    private String form;

    @Column(name = "manufacturer_name", columnDefinition = "text")
    private String manufacturerName;

    @Column(name = "drug_class", columnDefinition = "text")
    private String drugClass;

    @Column(name = "unit_of_measure", columnDefinition = "text")
    private String unitOfMeasure;

    @Column(name = "quantity_per_pack", precision = 12, scale = 3)
    private BigDecimal quantityPerPack;
}