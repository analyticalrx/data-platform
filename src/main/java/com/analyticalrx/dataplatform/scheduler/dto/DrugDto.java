package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DrugDto(
        @JsonProperty("LotNo") String lotNo,
        @JsonProperty("ExpiryDate") String expiryDate,
        @JsonProperty("ManufacturerName") String manufacturerName,
        @JsonProperty("Form") String form,
        @JsonProperty("DrugName") String drugName,
        @JsonProperty("Class") String drugClass,
        @JsonProperty("NDC") String ndc,
        @JsonProperty("SpecificProductID") Long specificProductId,
        @JsonProperty("Strong") String strong,
        @JsonProperty("StrengthUM") String strengthUm,
        @JsonProperty("Strength") String strength
) {
}
