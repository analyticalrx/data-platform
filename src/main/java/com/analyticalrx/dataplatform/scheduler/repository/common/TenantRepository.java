package com.analyticalrx.dataplatform.scheduler.repository.common;

import com.analyticalrx.dataplatform.scheduler.entity.schema.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {

    Optional<TenantEntity> findByTenantKey(String tenantKey);

    boolean existsByTenantKey(String tenantKey);
}