package com.analyticalrx.dataplatform.scheduler.constants.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum PrimeRxEventType {

    PATIENT_CREATED("PatientCreated"),
    PATIENT_INFO_UPDATED("PatientInfoUpdated"),
    RX_SAVED("RxSaved"),
    RX_DELIVERY("RxDelivery"),
    RX_BILLED("RxBilled"),
    RX_FILED("RxFiled"),
    RX_REVERSED("RxReversed"),
    LABEL_PRINTED("LabelPrinted"),
    DPV_DONE("DPVDone"),
    RPH_V_DONE("RphVDone"),
    RX_SHIPPED("RxShipped"),
    RX_TRANSMISSION_REJECTED("RxTransmissionRejected"),
    RX_DELETED("RxDeleted"),
    ERX_RECEIVED("ERxReceived"),
    WORKFLOW_TRANSITION("WorkflowTransition");

    private final String value;

    PrimeRxEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, PrimeRxEventType> LOOKUP =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(
                            PrimeRxEventType::getValue,
                            Function.identity()
                    ));

    public static PrimeRxEventType fromValue(String value) {
        PrimeRxEventType eventType = LOOKUP.get(value);
        if (Objects.isNull(eventType)) throw new IllegalArgumentException("Unsupported PrimeRx event type: " + value);
        return eventType;
    }
}