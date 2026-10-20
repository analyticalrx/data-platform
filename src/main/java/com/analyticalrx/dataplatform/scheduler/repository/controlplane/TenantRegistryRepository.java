package com.analyticalrx.dataplatform.scheduler.repository.controlplane;

import com.analyticalrx.dataplatform.scheduler.dto.TenantRegistryDto;

import java.util.Optional;

public interface TenantRegistryRepository {
     Optional<TenantRegistryDto> findActiveTenant(String tenantKey);
}
