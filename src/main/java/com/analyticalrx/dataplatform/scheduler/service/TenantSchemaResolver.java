package com.analyticalrx.dataplatform.scheduler.service;

import com.analyticalrx.dataplatform.scheduler.dto.TenantRegistryDto;

public interface TenantSchemaResolver {
    TenantRegistryDto resolveSchema(String tenantKey);
}
