package com.analyticalrx.dataplatform.crm.followup;

import com.analyticalrx.dataplatform.crm.doctor_office.Priority;

import java.time.Instant;
import java.util.UUID;

public class FollowUpTaskPatchDTO {

    private String title;
    private String nextAction;
    private UUID assignedTo;
    private Instant dueAt;
    private TaskStatus status;
    private Priority priority;

    public FollowUpTaskPatchDTO() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }

    public UUID getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UUID assignedTo) { this.assignedTo = assignedTo; }

    public Instant getDueAt() { return dueAt; }
    public void setDueAt(Instant dueAt) { this.dueAt = dueAt; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}