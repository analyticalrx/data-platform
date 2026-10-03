package com.analyticalrx.dataplatform.pharmacy.repository;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.analyticalrx.dataplatform.auth.access.TenantSchemaResolver;
import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.pharmacy.model.PharmacyResponse;

@Repository
public class PharmacyRepository {
    private final JdbcTemplate jdbcTemplate;
    private final TenantSchemaResolver tenantSchemaResolver;

    public PharmacyRepository(JdbcTemplate jdbcTemplate, TenantSchemaResolver tenantSchemaResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.tenantSchemaResolver = tenantSchemaResolver;
    }

    public Optional<PharmacyResponse> findActiveByKey(PlatformUserDetails user, String pharmacyKey) {
        String schemaName = tenantSchemaResolver.findActiveSchemaName(user);
        String sql = """
                select pharmacy_key, display_name, state_code, npi, ncpdp
                from %s.pharmacy
                where tenant_id = ? and pharmacy_key = ? and status = 'active'
                """.formatted(schemaName);

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PharmacyResponse(
                rs.getString("pharmacy_key"),
                rs.getString("display_name"),
                rs.getString("state_code"),
                rs.getString("npi"),
                rs.getString("ncpdp")), user.tenantId(), pharmacyKey).stream().findFirst();
    }
}
