package com.analyticalrx.dataplatform.scheduler.handler.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.service.ingestion.PatientService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PatientCreatedHandler implements PrimeRxEventHandler {

    private final PatientService patientService;

    @Override
    public PrimeRxEventType getEventType() {
        return PrimeRxEventType.PATIENT_CREATED;
    }

    @Override
    public void process(Meta meta, JsonNode payload) {

        JsonNode patient = payload.path("Patient");
        JsonNode pharmacy = payload.path("Pharmacy");

        if (!patient.isObject() || !pharmacy.isObject()) {
            throw new IllegalArgumentException(
                    "Missing Patient or Pharmacy object"
            );
        }

        String patientNo =
                patient.path("PatientNo").asText(null);

        String pharmacyId =
                pharmacy.path("PharmacyId").asText(null);

        if (patientNo == null || pharmacyId == null) {
            throw new IllegalArgumentException(
                    "Missing patient or pharmacy identifier"
            );
        }

        log.info(
                "Processing PatientCreated. requestId={}, pharmacyId={}",
                meta.getRequestId(),
                pharmacyId
        );

//        patientService.createPatient(
//                meta,
//                patient,
//                pharmacyId
//        );
    }
}