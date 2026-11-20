package com.analyticalrx.dataplatform.crm.followup;

import com.analyticalrx.dataplatform.crm.doctor_office.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public class FollowUpTaskRequestDTO {

    @NotNull(message = "Office ID is required")
    private UUID officeId;

    private UUID interactionId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Activity type is required")
    private ActivityType activityType;

    @NotBlank(message = "Next action details are required")
    private String nextAction;

    @NotNull(message = "Assignee ID is required")
    private UUID assignedTo;

    @NotNull(message = "Due timestamp is required")
    private Instant dueAt;

    private Priority priority;

    @NotNull(message = "Creator ID is required")
    private UUID createdBy;

    public FollowUpTaskRequestDTO() {}

    // Getters and Setters
    public UUID getOfficeId() { return officeId; }
    public void setOfficeId(UUID officeId) { this.officeId = officeId; }

    public UUID getInteractionId() { return interactionId; }
    public void setInteractionId(UUID interactionId) { this.interactionId = interactionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }

    public UUID getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UUID assignedTo) { this.assignedTo = assignedTo; }

    public Instant getDueAt() { return dueAt; }
    public void setDueAt(Instant dueAt) { this.dueAt = dueAt; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}