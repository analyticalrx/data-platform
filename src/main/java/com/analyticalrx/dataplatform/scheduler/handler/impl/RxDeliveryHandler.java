package com.analyticalrx.dataplatform.scheduler.handler.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.service.ingestion.PrescriptionDeliveryService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RxDeliveryHandler implements PrimeRxEventHandler {

    private final PrescriptionDeliveryService deliveryService;

    @Override
    public PrimeRxEventType getEventType() {
        return PrimeRxEventType.RX_DELIVERY;
    }

    @Override
    public void process(Meta meta, JsonNode payload) {

//        deliveryService.prepareDelivery(meta, payload);
    }
}