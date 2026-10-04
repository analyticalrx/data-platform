package com.analyticalrx.dataplatform.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;

/** Prescription event data; source dates and Y/N flags retain their original string values. */
public record PrescriptionPayloadDto(
        @JsonProperty("TotalRxAmount") BigDecimal totalRxAmount,
        @JsonProperty("PrimaryInsurancePaid") BigDecimal primaryInsurancePaid,
        @JsonProperty("QtyOrdered") BigDecimal qtyOrdered,
        @JsonProperty("FollowupTag") String followupTag,
        @JsonProperty("RefillNo") Integer refillNo,
        @JsonProperty("PatientCopay") BigDecimal patientCopay,
        @JsonProperty("SecondaryInsurancePaid") BigDecimal secondaryInsurancePaid,
        @JsonProperty("DateOrdered") String dateOrdered,
        @JsonProperty("AuthRefills") Integer authRefills,
        @JsonProperty("PharmVerificationDone") String pharmVerificationDone,
        @JsonProperty("EPrescriptionData") EPrescriptionDataDto ePrescriptionData,
        @JsonProperty("CopayPaid") String copayPaid,
        @JsonProperty("Prescriber") PrescriberDto prescriber,
        @JsonProperty("DaysSupply") Integer daysSupply,
        @JsonProperty("PickedUp") String pickedUp,
        @JsonProperty("QtyDispensed") BigDecimal qtyDispensed,
        @JsonProperty("QtyRemaining") BigDecimal qtyRemaining,
        @JsonProperty("TimePicked") String timePicked,
        @JsonProperty("Diagnosis") List<DiagnosisDto> diagnosis,
        @JsonProperty("QueueName") String queueName,
        @JsonProperty("RxNo") Long rxNo,
        @JsonProperty("Status") String status,
        @JsonProperty("BillType") String billType,
        @JsonProperty("Drug") DrugDto drug,
        @JsonProperty("DeliveryMethod") String deliveryMethod,
        @JsonProperty("DatePicked") String datePicked,
        @JsonProperty("Patient") PatientDto patient,
        // The sample has no payment entries, so their schema is not yet known.
        @JsonProperty("RxPayRecords") List<JsonNode> rxPayRecords,
        @JsonProperty("DateFilled") String dateFilled,
        @JsonProperty("RxOriginCode") String rxOriginCode,
        @JsonProperty("TertiaryInsurancePaid") BigDecimal tertiaryInsurancePaid,
        @JsonProperty("LabelPrinted") String labelPrinted,
        @JsonProperty("Sig") String sig,
        @JsonProperty("RxOriginDescription") String rxOriginDescription,
        @JsonProperty("Pharmacy") PharmacyDto pharmacy,
        @JsonProperty("Event") String event,
        @JsonProperty("PrescriptionNotes") String prescriptionNotes
) {
}
