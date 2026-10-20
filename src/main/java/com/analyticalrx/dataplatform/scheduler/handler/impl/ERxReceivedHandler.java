package com.analyticalrx.dataplatform.scheduler.handler.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.service.ingestion.ElectronicPrescriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ERxReceivedHandler implements PrimeRxEventHandler {

    private final ElectronicPrescriptionService electronicPrescriptionService;

    @Override
    public PrimeRxEventType getEventType() {
        return PrimeRxEventType.ERX_RECEIVED;
    }

    @Override
    public void process(Meta meta, JsonNode payload) {

//        electronicPrescriptionService.processReceived(meta, payload);
    }
}