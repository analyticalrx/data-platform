package com.analyticalrx.dataplatform.crm.followup;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FollowUpTaskService {
    FollowUpTaskResponseDTO createTask(FollowUpTaskRequestDTO requestDTO);
    FollowUpTaskResponseDTO updateTask(UUID id, FollowUpTaskPatchDTO patchDTO);
    FollowUpTaskResponseDTO getTaskById(UUID id);
    List<FollowUpTaskResponseDTO> getTasks(UUID assignedTo, TaskStatus status, Boolean overdue, UUID officeId);
    List<FollowUpTaskResponseDTO> getCalendarActivities(Instant start, Instant end);
}