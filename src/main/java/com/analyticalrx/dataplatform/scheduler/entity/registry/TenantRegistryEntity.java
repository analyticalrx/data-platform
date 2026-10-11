package com.analyticalrx.dataplatform.scheduler.entity.registry;

import com.analyticalrx.dataplatform.scheduler.constants.enums.TenantStatus;
import com.analyticalrx.dataplatform.scheduler.entity.audit.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "tenant_registry",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_tenant_registry_tenant_key",
            columnNames = "tenant_key"
        ),
        @UniqueConstraint(
            name = "uk_tenant_registry_schema_name",
            columnNames = "schema_name"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantRegistryEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "tenant_key", nullable = false, unique = true)
    private String tenantKey;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "schema_name", nullable = false, unique = true)
    private String schemaName;

    @Column(name = "status", nullable = false)
    private TenantStatus status = TenantStatus.active;
}