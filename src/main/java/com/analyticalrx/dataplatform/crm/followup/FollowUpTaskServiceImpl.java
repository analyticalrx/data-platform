package com.analyticalrx.dataplatform.crm.followup;

import com.analyticalrx.dataplatform.crm.doctor_office.DoctorOfficeRepository;
import com.analyticalrx.dataplatform.crm.doctor_office.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FollowUpTaskServiceImpl implements FollowUpTaskService {

    private final FollowUpTaskRepository repository;
    private final DoctorOfficeRepository officeRepository;

    @Autowired
    public FollowUpTaskServiceImpl(FollowUpTaskRepository repository, DoctorOfficeRepository officeRepository) {
        this.repository = repository;
        this.officeRepository = officeRepository;
    }

    @Override
    public FollowUpTaskResponseDTO createTask(FollowUpTaskRequestDTO dto) {
        if (!officeRepository.existsById(dto.getOfficeId())) {
            throw new RuntimeException("Doctor office not found with id: " + dto.getOfficeId());
        }

        FollowUpTask task = new FollowUpTask();
        task.setOfficeId(dto.getOfficeId());
        task.setInteractionId(dto.getInteractionId());
        task.setTitle(dto.getTitle());
        task.setActivityType(dto.getActivityType());
        task.setNextAction(dto.getNextAction());
        task.setAssignedTo(dto.getAssignedTo());
        task.setDueAt(dto.getDueAt());
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : Priority.NORMAL);
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(dto.getCreatedBy());

        return mapToDTO(repository.save(task));
    }

    @Override
    public FollowUpTaskResponseDTO updateTask(UUID id, FollowUpTaskPatchDTO patchDTO) {
        FollowUpTask task = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Follow-up task not found with id: " + id));

        if (patchDTO.getTitle() != null) task.setTitle(patchDTO.getTitle());
        if (patchDTO.getNextAction() != null) task.setNextAction(patchDTO.getNextAction());
        if (patchDTO.getAssignedTo() != null) task.setAssignedTo(patchDTO.getAssignedTo());
        if (patchDTO.getDueAt() != null) task.setDueAt(patchDTO.getDueAt());
        if (patchDTO.getPriority() != null) task.setPriority(patchDTO.getPriority());

        if (patchDTO.getStatus() != null) {
            task.setStatus(patchDTO.getStatus());
            if (patchDTO.getStatus() == TaskStatus.COMPLETED) {
                task.setCompletedAt(Instant.now());
            } else {
                task.setCompletedAt(null);
            }
        }

        return mapToDTO(repository.save(task));
    }

    @Override
    public FollowUpTaskResponseDTO getTaskById(UUID id) {
        FollowUpTask task = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Follow-up task not found with id: " + id));
        return mapToDTO(task);
    }

    @Override
    public List<FollowUpTaskResponseDTO> getTasks(UUID assignedTo, TaskStatus status, Boolean overdue, UUID officeId) {
        List<FollowUpTask> tasks;

        if (Boolean.TRUE.equals(overdue) && assignedTo != null) {
            tasks = repository.findOverdueTasks(assignedTo, Arrays.asList(TaskStatus.OPEN, TaskStatus.IN_PROGRESS), Instant.now());
        } else if (officeId != null) {
            tasks = repository.findByOfficeId(officeId);
        } else if (assignedTo != null) {
            List<TaskStatus> statuses = status != null ? List.of(status) : Arrays.asList(TaskStatus.OPEN, TaskStatus.IN_PROGRESS);
            tasks = repository.findByAssignedToAndStatusIn(assignedTo, statuses);
        } else {
            tasks = repository.findAll();
        }

        return tasks.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<FollowUpTaskResponseDTO> getCalendarActivities(Instant start, Instant end) {
        return repository.findByDueAtBetween(start, end)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private FollowUpTaskResponseDTO mapToDTO(FollowUpTask task) {
        FollowUpTaskResponseDTO dto = new FollowUpTaskResponseDTO();
        dto.setId(task.getId());
        dto.setOfficeId(task.getOfficeId());
        dto.setInteractionId(task.getInteractionId());
        dto.setTitle(task.getTitle());
        dto.setActivityType(task.getActivityType());
        dto.setNextAction(task.getNextAction());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setDueAt(task.getDueAt());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }
}