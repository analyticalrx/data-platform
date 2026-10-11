package com.analyticalrx.dataplatform.scheduler.repository.registry;

import com.analyticalrx.dataplatform.scheduler.entity.registry.TenantRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRegistryRepository extends JpaRepository<TenantRegistryEntity, Long> {

    Optional<TenantRegistryEntity> findByTenantKey(String tenantKey);

    Optional<TenantRegistryEntity> findBySchemaName(String schemaName);

    boolean existsByTenantKey(String tenantKey);

    boolean existsBySchemaName(String schemaName);
}