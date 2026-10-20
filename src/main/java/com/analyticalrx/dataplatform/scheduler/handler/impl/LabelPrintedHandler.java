package com.analyticalrx.dataplatform.scheduler.handler.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.service.ingestion.PrescriptionWorkflowService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LabelPrintedHandler implements PrimeRxEventHandler {

    private final PrescriptionWorkflowService workflowService;

    @Override
    public PrimeRxEventType getEventType() {
        return PrimeRxEventType.LABEL_PRINTED;
    }

    @Override
    public void process(Meta meta, JsonNode payload) {

//        workflowService.markLabelPrinted(meta, payload);
    }
}