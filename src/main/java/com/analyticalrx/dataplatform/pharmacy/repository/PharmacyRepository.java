package com.analyticalrx.dataplatform.pharmacy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.analyticalrx.dataplatform.auth.access.TenantSchemaResolver;
import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.security.PharmacyAccessGuard;
import com.analyticalrx.dataplatform.pharmacy.model.PharmacyResponse;

@Repository
public class PharmacyRepository {
    private final JdbcTemplate jdbcTemplate;
    private final TenantSchemaResolver tenantSchemaResolver;
    private final PharmacyAccessGuard pharmacyAccessGuard;
    private static final RowMapper<PharmacyResponse> PHARMACY_MAPPER = (rs, rowNum) -> new PharmacyResponse(
            rs.getString("pharmacy_key"),
            rs.getString("display_name"),
            rs.getString("state_code"),
            rs.getString("npi"),
            rs.getString("ncpdp"));

    public PharmacyRepository(JdbcTemplate jdbcTemplate, TenantSchemaResolver tenantSchemaResolver,
                              PharmacyAccessGuard pharmacyAccessGuard) {
        this.jdbcTemplate = jdbcTemplate;
        this.tenantSchemaResolver = tenantSchemaResolver;
        this.pharmacyAccessGuard = pharmacyAccessGuard;
    }

    public Optional<PharmacyResponse> findActiveByKey(PlatformUserDetails user, String pharmacyKey) {
        pharmacyAccessGuard.requirePharmacyAccess(user, pharmacyKey);
        String schemaName = tenantSchemaResolver.findActiveSchemaName(user);
        String sql = """
                select pharmacy_key, display_name, state_code, npi, ncpdp
                from %s.pharmacy
                where tenant_id = ? and pharmacy_key = ? and status = 'active'
                """.formatted(schemaName);

        return jdbcTemplate.query(sql, PHARMACY_MAPPER, user.tenantId(), pharmacyKey).stream().findFirst();
    }

    public List<PharmacyResponse> findAccessible(PlatformUserDetails user) {
        pharmacyAccessGuard.requireActiveRole(user);
        String schemaName = tenantSchemaResolver.findActiveSchemaName(user);
        if (pharmacyAccessGuard.hasTenantWideAccess(user)) {
            String sql = """
                    select p.pharmacy_key, p.display_name, p.state_code, p.npi, p.ncpdp
                    from %s.pharmacy p
                    where p.tenant_id = ? and p.status = 'active'
                    order by p.display_name, p.pharmacy_key
                    """.formatted(schemaName);
            return jdbcTemplate.query(sql, PHARMACY_MAPPER, user.tenantId());
        }

        String sql = """
                select p.pharmacy_key, p.display_name, p.state_code, p.npi, p.ncpdp
                from %s.pharmacy p
                join control_plane.user_pharmacy_access pa
                  on pa.tenant_id = p.tenant_id and pa.pharmacy_key = p.pharmacy_key
                where p.tenant_id = ? and p.status = 'active'
                  and pa.user_id = ? and pa.status = 'active'
                order by p.display_name, p.pharmacy_key
                """.formatted(schemaName);
        return jdbcTemplate.query(sql, PHARMACY_MAPPER, user.tenantId(), user.id());
    }
}
