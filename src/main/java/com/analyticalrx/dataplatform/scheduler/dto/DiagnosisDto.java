package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiagnosisDto(
        @JsonProperty("Description") String description,
        @JsonProperty("Code") String code
) {
}
