package com.analyticalrx.dataplatform.task.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddTaskCommentRequest(@NotBlank @Size(max = 2000) String commentText) {
}
