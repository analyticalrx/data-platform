package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record EventEnvelope(String eventId, String eventType, JsonNode payload) {
}