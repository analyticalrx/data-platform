package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Patient values remain strings because the source sample contains hashed data. */
public record PatientDto(
        @JsonProperty("MobileNo") String mobileNo,
        @JsonProperty("Email") String email,
        @JsonProperty("Category") String category,
        @JsonProperty("Address") AddressDto address,
        @JsonProperty("WorkPhone") String workPhone,
        @JsonProperty("FirstName") String firstName,
        @JsonProperty("DriverLicenseNumber") String driverLicenseNumber,
        @JsonProperty("ExternalId") String externalId,
        @JsonProperty("MiddleInitials") String middleInitials,
        @JsonProperty("Gender") String gender,
        @JsonProperty("PatientNo") String patientNo,
        @JsonProperty("Language") String language,
        @JsonProperty("DOB") String dob,
        @JsonProperty("Phone") String phone,
        @JsonProperty("DriverLicenseExpiry") String driverLicenseExpiry,
        @JsonProperty("LastName") String lastName
) {
}
