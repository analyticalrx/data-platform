package com.analyticalrx.dataplatform.scheduler.handler;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.fasterxml.jackson.databind.JsonNode;

public interface PrimeRxEventHandler {

    PrimeRxEventType getEventType();

    void process(Meta meta, JsonNode payload);
}