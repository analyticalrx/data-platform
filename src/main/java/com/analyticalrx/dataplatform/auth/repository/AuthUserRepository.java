package com.analyticalrx.dataplatform.auth.repository;

import java.util.LinkedHashSet;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Repository;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;

@Repository
public class AuthUserRepository {
    private final JdbcTemplate jdbc;

    public AuthUserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<PlatformUserDetails> findById(long id) {
        return find(" u.id = ?", id);
    }

    private Optional<PlatformUserDetails> find(String predicate, Object value) {
        String sql = """
                select u.id, u.tenant_id, u.email, u.display_name, u.password_hash,
                       u.status, u.permissions_version, r.role_code, p.permission_code
                from control_plane.app_user u
                left join control_plane.user_role ur on ur.user_id = u.id and ur.status = 'active'
                left join control_plane.role_definition r on r.id = ur.role_id and r.status = 'active'
                left join control_plane.role_permission rp on rp.role_id = r.id
                left join control_plane.permission_definition p on p.id = rp.permission_id and p.status = 'active'
                where""" + predicate;
        var rows = jdbc.queryForList(sql, value);
        if (rows.isEmpty()) return Optional.empty();
        var first = rows.getFirst();
        var roles = new LinkedHashSet<String>();
        var permissions = new LinkedHashSet<String>();
        for (var row : rows) {
            if (row.get("role_code") != null) roles.add((String) row.get("role_code"));
            if (row.get("permission_code") != null) permissions.add((String) row.get("permission_code"));
        }
        var authorities = permissions.stream().map(SimpleGrantedAuthority::new).toList();
        return Optional.of(new PlatformUserDetails(
                ((Number) first.get("id")).longValue(),
                (Long) first.get("tenant_id"),
                (String) first.get("email"),
                (String) first.get("display_name"),
                (String) first.get("password_hash"),
                (String) first.get("status"),
                ((Number) first.get("permissions_version")).intValue(),
                roles, permissions, authorities));
    }

    public Optional<PlatformUserDetails> findByTenantKeyAndEmail(String tenantKey, String email){
        String sql = """
                select t.id as tenant_id, t.tenant_key, u.id as user_id, u.email, u.display_name, u.password_hash, u.permissions_version
                from control_plane.tenant_registry t
                join control_plane.app_user u on u.tenant_id = t.id
                where t.tenant_key = ? and lower(u.email) = lower(?) and t.status = 'active' and u.status = 'active'
                """;
        var rows = jdbc.queryForList(sql, tenantKey, email);
        if (rows.isEmpty()) return Optional.empty();
        var result = rows.getFirst();
        var userId = ((Number) result.get("user_id")).longValue();
        return findById(userId);
    }
}
