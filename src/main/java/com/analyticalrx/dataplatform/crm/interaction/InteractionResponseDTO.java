package com.analyticalrx.dataplatform.crm.interaction;

import java.time.Instant;
import java.util.UUID;

public class InteractionResponseDTO {

    private UUID id;
    private UUID officeId;
    private Instant occurredAt;
    private UUID visitorId;
    private InteractionType interactionType;
    private String purpose;
    private UUID pitchId;
    private String pitchSnapshot;
    private String objections;
    private InteractionOutcome outcome;
    private String notes;
    private Instant createdAt;

    public InteractionResponseDTO() {}

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
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}