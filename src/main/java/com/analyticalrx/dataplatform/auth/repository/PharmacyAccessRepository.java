package com.analyticalrx.dataplatform.auth.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PharmacyAccessRepository {
    private final JdbcTemplate jdbcTemplate;

    public PharmacyAccessRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasActiveAssignment(long userId, long tenantId, String pharmacyKey) {
        String sql = """
                select exists (
                    select 1 from control_plane.user_pharmacy_access pa
                    where pa.user_id = ?
                      and pa.tenant_id = ?
                      and pa.pharmacy_key = ?
                      and pa.status = 'active'
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId, tenantId, pharmacyKey));
    }
}
