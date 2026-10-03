package com.analyticalrx.dataplatform.auth.repository;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository 
public class TenantRegistryRepository {
    private final JdbcTemplate jdbcTemplate;

    public TenantRegistryRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<String> findActiveSchemaName(long tenantId){
        String sql = """
                select schema_name from control_plane.tenant_registry where id = ? and status = 'active'
                """;
        return jdbcTemplate.queryForList(sql, String.class, tenantId).stream().findFirst();

    }
}
