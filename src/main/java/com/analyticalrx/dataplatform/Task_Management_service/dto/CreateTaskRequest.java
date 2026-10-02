package com.primerx.task.dto;

import com.primerx.task.domain.TaskPriority;
import java.time.OffsetDateTime;

public class CreateTaskRequest {

    private String title;
    private String description;
    private TaskPriority priority;
    private OffsetDateTime dueAt;
    private String assigneeId;
    private String requiredRole;
    private String pharmacyKey;

    public CreateTaskRequest() {
    }

    public CreateTaskRequest(String title, String description, TaskPriority priority, OffsetDateTime dueAt, String assigneeId, String requiredRole, String pharmacyKey) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueAt = dueAt;
        this.assigneeId = assigneeId;
        this.requiredRole = requiredRole;
        this.pharmacyKey = pharmacyKey;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public OffsetDateTime getDueAt() { return dueAt; }
    public void setDueAt(OffsetDateTime dueAt) { this.dueAt = dueAt; }

    public String getAssigneeId() { return assigneeId; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }

    public String getRequiredRole() { return requiredRole; }
    public void setRequiredRole(String requiredRole) { this.requiredRole = requiredRole; }

    public String getPharmacyKey() { return pharmacyKey; }
    public void setPharmacyKey(String pharmacyKey) { this.pharmacyKey = pharmacyKey; }
}