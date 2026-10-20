package com.analyticalrx.dataplatform.scheduler.service.impl;

import com.analyticalrx.dataplatform.scheduler.dto.TenantRegistryDto;
import com.analyticalrx.dataplatform.scheduler.repository.controlplane.TenantRegistryRepository;
import com.analyticalrx.dataplatform.scheduler.service.TenantSchemaResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TenantSchemaResolverImpl implements TenantSchemaResolver {

    private final ConcurrentMap<String, TenantRegistryDto> schemaCache = new ConcurrentHashMap<>();
    private static final Pattern SCHEMA_PATTERN = Pattern.compile("^tenant_[a-z0-9_]+$");
    private final TenantRegistryRepository tenantRegistryRepository;

    public TenantSchemaResolverImpl(TenantRegistryRepository tenantRegistryRepository) {
        this.tenantRegistryRepository = tenantRegistryRepository;
    }

    @Override
    public TenantRegistryDto resolveSchema(String tenantKey) {
        if (Objects.isNull(tenantKey) || tenantKey.isBlank()) {
            throw new IllegalArgumentException("Tenant key cannot be null or empty");
        }

        return schemaCache.computeIfAbsent(tenantKey, this::fetchSchemaNameFromDatabase);
    }

    @Transactional(readOnly = true)
    private TenantRegistryDto fetchSchemaNameFromDatabase(String tenantKey) {

        try {
            TenantRegistryDto tenant = tenantRegistryRepository.findActiveTenant(tenantKey)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Unknown or inactive tenant: " + tenantKey
                            )
                    );

            String schema = tenant.schemaName();
            if (Objects.isNull(schema) || !SCHEMA_PATTERN.matcher(schema).matches()) {
                throw new IllegalStateException("Invalid schema for tenant: " + tenantKey);
            }
            return tenant;
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Unknown or inactive tenant: " + tenantKey, e);
        }
    }
}
