package com.analyticalrx.dataplatform.crm.interaction;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public class InteractionRequestDTO {

    @NotNull(message = "Occurred timestamp is required")
    private Instant occurredAt;

    @NotNull(message = "Visitor ID is required")
    private UUID visitorId;

    @NotNull(message = "Interaction type is required")
    private InteractionType interactionType;

    @NotNull(message = "Purpose is required")
    private String purpose;

    private UUID pitchId;
    private String pitchSnapshot;
    private String objections;

    @NotNull(message = "Outcome is required")
    private InteractionOutcome outcome;

    private String notes;

    public InteractionRequestDTO() {}

    // Getters and Setters
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
}