package com.analyticalrx.dataplatform.scheduler.handler.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.service.ingestion.PrescriptionBillingService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RxReversedHandler implements PrimeRxEventHandler {

    private final PrescriptionBillingService billingService;

    @Override
    public PrimeRxEventType getEventType() {
        return PrimeRxEventType.RX_REVERSED;
    }

    @Override
    public void process(Meta meta, JsonNode payload) {

//        billingService.processReversal(meta, payload);
    }
}