package com.analyticalrx.dataplatform.scheduler.handler.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.service.ingestion.PrescriptionWorkflowService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowTransitionHandler implements PrimeRxEventHandler {

    private final PrescriptionWorkflowService workflowService;

    public PrimeRxEventType getEventType() {
        return PrimeRxEventType.WORKFLOW_TRANSITION;
    }

    @Override
    public void process(Meta meta, JsonNode payload) {

        String rxNo = payload.path("RxNo").asText(null);
        String queueName = payload.path("QueueName").asText(null);
        String pharmacyId = payload.path("Pharmacy")
                .path("PharmacyId")
                .asText(null);

        if (rxNo == null || queueName == null || pharmacyId == null) {
            throw new IllegalArgumentException(
                    "Missing required workflow fields"
            );
        }

        log.info(
                "WorkflowTransition: rxNo={}, queue={}, requestId={}",
                rxNo,
                queueName,
                meta.getRequestId()
        );

//        workflowService.processTransition(meta, payload);
    }
}