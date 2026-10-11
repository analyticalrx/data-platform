package com.analyticalrx.dataplatform.scheduler.service.impl;

import com.analyticalrx.dataplatform.scheduler.service.TenantSchemaResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TenantSchemaResolverImpl implements TenantSchemaResolver {

    private final JdbcTemplate jdbcTemplate;
    private final ConcurrentMap<String, String> schemaCache = new ConcurrentHashMap<>();
    private static final Pattern SCHEMA_PATTERN = Pattern.compile("^tenant_[a-z0-9_]+$");

    @Autowired
    public TenantSchemaResolverImpl(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String resolveSchema(String tenantKey) {
        if (Objects.isNull(tenantKey) || tenantKey.isBlank()) {
            throw new IllegalArgumentException("Tenant key cannot be null or empty");
        }

        return schemaCache.computeIfAbsent(tenantKey, this::fetchSchemaNameFromDatabase);
    }

    private String fetchSchemaNameFromDatabase(String tenantKey) {

        try {
            String schema = jdbcTemplate.queryForObject(
                    """
                            SELECT schema_name
                            FROM public.tenant_registry
                            WHERE tenant_key = ?
                              AND status = 'active'
                            """,
                    String.class,
                    tenantKey
            );

            if (Objects.isNull(schema) || !SCHEMA_PATTERN.matcher(schema).matches()) {
                throw new IllegalStateException("Invalid schema for tenant: " + tenantKey);
            }
            return schema;
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Unknown or inactive tenant: " + tenantKey, e);
        }
    }
}
