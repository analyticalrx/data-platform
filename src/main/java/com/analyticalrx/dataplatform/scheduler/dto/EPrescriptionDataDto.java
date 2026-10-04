package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EPrescriptionDataDto(
        @JsonProperty("SenderSoftwareProductName") String senderSoftwareProductName,
        @JsonProperty("PrescriberOrderReferenceNumber") String prescriberOrderReferenceNumber,
        @JsonProperty("PrescriberClinicName") String prescriberClinicName,
        @JsonProperty("MessageID") String messageId
) {
}
