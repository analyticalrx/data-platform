package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PharmacyDto(
        @JsonProperty("PharmacyId") String pharmacyId,
        @JsonProperty("Zip") String zip,
        @JsonProperty("NPI") String npi,
        @JsonProperty("Phone") String phone,
        @JsonProperty("State") String state,
        @JsonProperty("Address1") String address1,
        @JsonProperty("City") String city,
        @JsonProperty("NCPDP") String ncpdp,
        @JsonProperty("Name") String name
) {
}
