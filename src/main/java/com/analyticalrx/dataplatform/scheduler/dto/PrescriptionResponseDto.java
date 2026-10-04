package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PrescriptionResponseDto(
        @JsonProperty("payload") PrescriptionPayloadDto payload,
        @JsonProperty("meta") Meta meta
) {
}
