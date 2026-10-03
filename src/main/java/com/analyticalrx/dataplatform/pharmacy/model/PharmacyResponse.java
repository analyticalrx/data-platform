package com.analyticalrx.dataplatform.pharmacy.model;

public record PharmacyResponse(
        String pharmacyKey,
        String displayName,
        String stateCode,
        String npi,
        String ncpdp
) {}
