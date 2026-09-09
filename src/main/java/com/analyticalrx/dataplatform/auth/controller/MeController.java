package com.analyticalrx.dataplatform.auth.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {
    private final JdbcTemplate jdbc;
    public MeController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public Map<String, Object> me(@AuthenticationPrincipal PlatformUserDetails user) {
        var pharmacies = jdbc.queryForList("""
                select pharmacy_key from control_plane.user_pharmacy_access
                where user_id = ? and tenant_id = ? and status = 'active'
                order by pharmacy_key
                """, user.id(), user.tenantId()).stream()
                .map(row -> row.get("pharmacy_key")).toList();
        return Map.of(
                "user", Map.of("id", user.id(), "email", user.email(), "displayName", user.displayName(), "status", user.status()),
                "tenantId", user.tenantId(), "roles", user.roles(), "permissions", user.permissions(),
                "pharmacyKeys", pharmacies, "permissionsVersion", user.permissionsVersion());
    }
}
