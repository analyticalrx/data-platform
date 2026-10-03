package com.analyticalrx.dataplatform.auth.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.repository.PharmacyAccessRepository;

@Component
public class PharmacyAccessGuard {
    private final PharmacyAccessRepository pharmacyAccessRepository;

    public PharmacyAccessGuard(PharmacyAccessRepository pharmacyAccessRepository) {
        this.pharmacyAccessRepository = pharmacyAccessRepository;
    }

    public boolean hasTenantWideAccess(PlatformUserDetails user) {
        return user != null && user.isEnabled() && !user.roles().isEmpty()
                && (user.roles().contains("OWNER") || user.roles().contains("TENANT_ADMIN"));
    }

    public void requireActiveRole(PlatformUserDetails user) {
        if (user == null || !user.isEnabled() || user.roles().isEmpty()) {
            throw new AccessDeniedException("Pharmacy access denied");
        }
    }

    public void requirePharmacyAccess(PlatformUserDetails user, String pharmacyKey) {
        requireActiveRole(user);
        if (pharmacyKey == null || pharmacyKey.isBlank()) {
            throw new AccessDeniedException("Pharmacy access denied");
        }

        if (hasTenantWideAccess(user)) {
            return;
        }

        if (pharmacyAccessRepository.hasActiveAssignment(user.id(), user.tenantId(), pharmacyKey)) {
            return;
        }

        throw new AccessDeniedException("Pharmacy access denied");
    }
}
