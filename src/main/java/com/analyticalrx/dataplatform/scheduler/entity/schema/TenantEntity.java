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
@Table(name = "tenant")
public class TenantEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_key", nullable = false, unique = true, columnDefinition = "text")
    private String tenantKey;

    @Column(name = "display_name", nullable = false, columnDefinition = "text")
    private String displayName;

    @Column(name = "status", nullable = false, columnDefinition = "text")
    private TenantStatus status = TenantStatus.active;
}