package com.analyticalrx.dataplatform.auth.security;


import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.repository.PharmacyAccessRepository;

@Component 
public class PharmacyAccessGuard {
    private final PharmacyAccessRepository pharmacyAccessRepository;

    public PharmacyAccessGuard(PharmacyAccessRepository pharmacyAccessRepository){
        this.pharmacyAccessRepository = pharmacyAccessRepository;
    }

    public void requirePharmacyAccess(PlatformUserDetails user, String pharmacyKey){

        if (user == null || pharmacyKey == null || pharmacyKey.isBlank()){
            throw new AccessDeniedException("Pharmacy Access denied");
        }
        if (user.roles().contains("OWNER")){
            return;
        }

        boolean staff = user.roles().contains("PHARMACIST") || user.roles().contains("TECHNICIAN");
        if (staff && pharmacyAccessRepository.hasActiveAssignment(user.id(), user.tenantId(), pharmacyKey)){
            return;
        }

        throw new AccessDeniedException("Pharmacy Access denied");
    }
}
