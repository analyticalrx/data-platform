package com.analyticalrx.dataplatform.crm.doctor_office;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "doctor_office")
public class DoctorOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "office_name", nullable = false)
    private String officeName;

    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    private String specialty;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String phone;

    @Column(name = "store_id")
    private UUID storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_stage", nullable = false)
    private RelationshipStage relationshipStage = RelationshipStage.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.NORMAL;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version = 0L;

    public DoctorOffice() {
    }

    public DoctorOffice(UUID id, String officeName, String doctorName, String specialty, 
                        String address, String phone, UUID storeId, 
                        RelationshipStage relationshipStage, Priority priority, UUID ownerId) {
        this.id = id;
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}