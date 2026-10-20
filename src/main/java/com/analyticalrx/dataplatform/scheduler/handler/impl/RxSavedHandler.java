package com.analyticalrx.dataplatform.scheduler.handler.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.service.ingestion.PrescriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RxSavedHandler implements PrimeRxEventHandler {

    private final PrescriptionService prescriptionService;

    @Override
    public PrimeRxEventType getEventType() {
        return PrimeRxEventType.RX_SAVED;
    }

    @Override
    public void process(Meta meta, JsonNode payload) {

        String rxNo = payload.path("RxNo").asText(null);

        String pharmacyId = payload.path("Pharmacy")
                .path("PharmacyId")
                .asText(null);

        if (rxNo == null || pharmacyId == null) {
            throw new IllegalArgumentException(
                    "Missing RxNo or PharmacyId"
            );
        }

        log.info(
                "Processing RxSaved. requestId={}, pharmacyId={}, rxNo={}",
                meta.getRequestId(),
                pharmacyId,
                rxNo
        );

//        prescriptionService.saveOrUpdate(
//                meta,
//                payload
//        );
    }
}