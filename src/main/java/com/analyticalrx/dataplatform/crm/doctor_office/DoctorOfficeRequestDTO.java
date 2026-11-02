package com.analyticalrx.dataplatform.crm.doctor_office;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class DoctorOfficeRequestDTO {

    @NotBlank(message = "Office name is required")
    private String officeName;

    @NotBlank(message = "Doctor name is required")
    private String doctorName;

    private String specialty;
    private String address;
    private String phone;
    private UUID storeId;
    private RelationshipStage relationshipStage;
    private Priority priority;
    private UUID ownerId;

    public DoctorOfficeRequestDTO() {
    }

    public DoctorOfficeRequestDTO(String officeName, String doctorName, String specialty, 
                                  String address, String phone, UUID storeId, 
                                  RelationshipStage relationshipStage, Priority priority, UUID ownerId) {
        this.officeName = officeName;
        this.doctorName = doctorName;
        this.specialty = specialty;
        this.address = address;
        this.phone = phone;
        this.storeId = storeId;
        this.relationshipStage = relationshipStage;
        this.priority = priority;
        this.ownerId = ownerId;
    }

    // Getters and Setters

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public RelationshipStage getRelationshipStage() {
        return relationshipStage;
    }

    public void setRelationshipStage(RelationshipStage relationshipStage) {
        this.relationshipStage = relationshipStage;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
}