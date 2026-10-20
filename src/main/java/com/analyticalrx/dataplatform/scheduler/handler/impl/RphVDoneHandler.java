package com.analyticalrx.dataplatform.scheduler.handler.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.service.ingestion.PrescriptionVerificationService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RphVDoneHandler implements PrimeRxEventHandler {

    private final PrescriptionVerificationService verificationService;

    @Override
    public PrimeRxEventType getEventType() {
        return PrimeRxEventType.RPH_V_DONE;
    }

    @Override
    public void process(Meta meta, JsonNode payload) {

//        verificationService.markPharmacistVerified(meta, payload);
    }
}