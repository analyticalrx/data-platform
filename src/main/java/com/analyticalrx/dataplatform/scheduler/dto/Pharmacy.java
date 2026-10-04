package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pharmacy {

    @JsonProperty("PharmacyId")
    private String pharmacyId;

    @JsonProperty("Zip")
    private String zip;

    @JsonProperty("NPI")
    private String npi;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("State")
    private String state;

    @JsonProperty("Address1")
    private String address1;

    @JsonProperty("City")
    private String city;

    @JsonProperty("NCPDP")
    private String ncpdp;

    @JsonProperty("Name")
    private String name;
}