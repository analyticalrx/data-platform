package com.analyticalrx.dataplatform.crm.interaction;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interaction")
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "office_id", nullable = false)
    private UUID officeId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "visitor_id", nullable = false)
    private UUID visitorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private InteractionType interactionType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String purpose;

    @Column(name = "pitch_id")
    private UUID pitchId;

    @Column(name = "pitch_snapshot", columnDefinition = "TEXT")
    private String pitchSnapshot;

    @Column(columnDefinition = "TEXT")
    private String objections;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionOutcome outcome;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Interaction() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOfficeId() { return officeId; }
    public void setOfficeId(UUID officeId) { this.officeId = officeId; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public UUID getVisitorId() { return visitorId; }
    public void setVisitorId(UUID visitorId) { this.visitorId = visitorId; }

    public InteractionType getInteractionType() { return interactionType; }
    public void setInteractionType(InteractionType interactionType) { this.interactionType = interactionType; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public UUID getPitchId() { return pitchId; }
    public void setPitchId(UUID pitchId) { this.pitchId = pitchId; }

    public String getPitchSnapshot() { return pitchSnapshot; }
    public void setPitchSnapshot(String pitchSnapshot) { this.pitchSnapshot = pitchSnapshot; }

    public String getObjections() { return objections; }
    public void setObjections(String objections) { this.objections = objections; }

    public InteractionOutcome getOutcome() { return outcome; }
    public void setOutcome(InteractionOutcome outcome) { this.outcome = outcome; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
}