package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AddressDto(
        @JsonProperty("State") String state,
        @JsonProperty("ZipCode") String zipCode,
        @JsonProperty("City") String city,
        @JsonProperty("StreetLine2") String streetLine2,
        @JsonProperty("StreetLine1") String streetLine1
) {
}
