package com.analyticalrx.dataplatform.scheduler.handler.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.service.ingestion.PrescriptionDeliveryService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RxShippedHandler implements PrimeRxEventHandler {

    private final PrescriptionDeliveryService deliveryService;

    @Override
    public PrimeRxEventType getEventType() {
        return PrimeRxEventType.RX_SHIPPED;
    }

    @Override
    public void process(Meta meta, JsonNode payload) {

//        deliveryService.markShipped(meta, payload);
    }
}