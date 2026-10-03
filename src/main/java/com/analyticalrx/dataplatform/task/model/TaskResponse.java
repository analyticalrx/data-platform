package com.analyticalrx.dataplatform.task.model;

import java.time.OffsetDateTime;

public record TaskResponse(
        long id,
        String pharmacyKey,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        OffsetDateTime dueAt,
        long assigneeUserId,
        long createdByUserId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime completedAt
) {
}
