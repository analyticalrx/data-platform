package com.analyticalrx.dataplatform.scheduler.service;

public interface TenantSchemaResolver {
    String resolveSchema(String tenantKey);
}
