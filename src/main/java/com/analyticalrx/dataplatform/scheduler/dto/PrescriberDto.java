package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PrescriberDto(
        @JsonProperty("MobileNo") String mobileNo,
        @JsonProperty("Email") String email,
        @JsonProperty("Address") AddressDto address,
        @JsonProperty("FirstName") String firstName,
        @JsonProperty("PresNo") String presNo,
        @JsonProperty("DEARegNo") String deaRegNo,
        @JsonProperty("LicenseNo") String licenseNo,
        @JsonProperty("Phone2") String phone2,
        @JsonProperty("Phone") String phone,
        @JsonProperty("FaxNo") String faxNo,
        @JsonProperty("MedicaidProvNo") String medicaidProvNo,
        @JsonProperty("NPINo") String npiNo,
        @JsonProperty("LastName") String lastName
) {
}
