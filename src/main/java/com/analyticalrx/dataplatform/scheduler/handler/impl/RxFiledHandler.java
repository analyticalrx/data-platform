package com.analyticalrx.dataplatform.scheduler.handler.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.service.ingestion.PrescriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RxFiledHandler implements PrimeRxEventHandler {

    private final PrescriptionService prescriptionService;

    @Override
    public PrimeRxEventType getEventType() {
        return PrimeRxEventType.RX_FILED;
    }

    @Override
    public void process(Meta meta, JsonNode payload) {
//        prescriptionService.markFiled(meta, payload);
    }
}