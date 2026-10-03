package com.analyticalrx.dataplatform.task.model;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String description,
        @NotBlank String pharmacyKey,
        @NotNull @Positive Long assigneeUserId,
        TaskPriority priority,
        OffsetDateTime dueAt
) {
}
