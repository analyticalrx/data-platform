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
public class Meta {

    @JsonProperty("processingStatus")
    private String processingStatus;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("source")
    private String source;

    @JsonProperty("receivedAt")
    private String receivedAt;
}
