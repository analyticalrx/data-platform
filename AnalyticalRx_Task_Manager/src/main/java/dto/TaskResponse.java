package dto;

import entity.Task;
import entity.TaskPriority;
import entity.TaskStatus;

import java.time.LocalDateTime;

public class TaskResponse {

    private Long id;
    private Long tenantId;
    private String pharmacyKey;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private String requiredRole;
    private Long assigneeId;
    private Long createdBy;
    private LocalDateTime dueAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TaskResponse(Task task) {

        this.id = task.getId();
        this.tenantId = task.getTenantId();
        this.pharmacyKey = task.getPharmacyKey();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.status = task.getStatus();
        this.requiredRole = task.getRequiredRole();
        this.assigneeId = task.getAssigneeId();
        this.createdBy = task.getCreatedBy();
        this.dueAt = task.getDueAt();
        this.completedAt = task.getCompletedAt();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getPharmacyKey() {
        return pharmacyKey;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}