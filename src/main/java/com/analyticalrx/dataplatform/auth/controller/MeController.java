package com.analyticalrx.dataplatform.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.pharmacy.model.PharmacyResponse;
import com.analyticalrx.dataplatform.pharmacy.repository.PharmacyRepository;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {
    private final PharmacyRepository pharmacyRepository;

    public MeController(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    @GetMapping
    public Map<String, Object> me(@AuthenticationPrincipal PlatformUserDetails user) {
        var pharmacyKeys = pharmacyRepository.findAccessible(user).stream()
                .map(PharmacyResponse::pharmacyKey).toList();
        return Map.of(
                "user", Map.of("id", user.id(), "email", user.email(), "displayName", user.displayName(), "status", user.status()),
                "tenantId", user.tenantId(), "roles", user.roles(), "permissions", user.permissions(),
                "pharmacyKeys", pharmacyKeys, "permissionsVersion", user.permissionsVersion());
    }
}
