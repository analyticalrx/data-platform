package com.analyticalrx.dataplatform.scheduler.repository.controlplane.impl;

import com.analyticalrx.dataplatform.scheduler.dto.TenantRegistryDto;
import com.analyticalrx.dataplatform.scheduler.repository.controlplane.TenantRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TenantRegistryRepositoryImpl implements TenantRegistryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<TenantRegistryDto> findActiveTenant(String tenantKey) {
        String sql = """
            SELECT tenant_key, schema_name, status
            FROM control_plane.tenant_registry
            WHERE tenant_key = ?
              AND status = 'active'
            """;

        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                            sql,
                            (rs, rowNum) -> new TenantRegistryDto(
                                    rs.getString("tenant_key"),
                                    rs.getString("schema_name"),
                                    rs.getString("status")
                            ),
                            tenantKey
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
