package com.analyticalrx.dataplatform.pharmacy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.security.PharmacyAccessGuard;
import com.analyticalrx.dataplatform.pharmacy.model.PharmacyResponse;
import com.analyticalrx.dataplatform.pharmacy.repository.PharmacyRepository;

@RestController
@RequestMapping("/api/v1/pharmacies")
public class PharmacyController {
    private final PharmacyAccessGuard pharmacyAccessGuard;
    private final PharmacyRepository pharmacyRepository;

    public PharmacyController(PharmacyAccessGuard pharmacyAccessGuard, PharmacyRepository pharmacyRepository) {
        this.pharmacyAccessGuard = pharmacyAccessGuard;
        this.pharmacyRepository = pharmacyRepository;
    }

    @GetMapping("/{pharmacyKey}")
    public PharmacyResponse getPharmacy(@AuthenticationPrincipal PlatformUserDetails user,
                                        @PathVariable String pharmacyKey) {
        pharmacyAccessGuard.requirePharmacyAccess(user, pharmacyKey);
        return pharmacyRepository.findActiveByKey(user, pharmacyKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found"));
    }
}
