package com.analyticalrx.dataplatform.task.model;

import java.time.OffsetDateTime;

public record TaskCommentResponse(
        long id,
        long taskId,
        long authorUserId,
        String commentText,
        OffsetDateTime createdAt
) {
}
