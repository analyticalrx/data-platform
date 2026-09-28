package service;

import dto.CreateTaskRequest;
import dto.UpdateTaskRequest;
import entity.Task;
import entity.TaskStatus;
import repository.TaskRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // =========================================================
    // GET TASKS
    // =========================================================

    public Page<Task> getTasks(
            Long tenantId,
            Pageable pageable) {

        return taskRepository.findByTenantId(
                tenantId,
                pageable
        );
    }

    // =========================================================
    // GET SINGLE TASK
    // =========================================================

    public Task getTask(
            Long tenantId,
            Long taskId) {

        return taskRepository
                .findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found")
                );
    }

    // =========================================================
    // CREATE TASK
    // =========================================================

    public Task createTask(
            CreateTaskRequest request,
            Long createdBy) {

        Task task = new Task();

        /*
         * For now we use tenantId from the request.
         *
         * Later, when security/authentication is connected,
         * tenantId should come from the authenticated user context.
         */
        task.setTenantId(request.getTenantId());

        task.setPharmacyKey(
                request.getPharmacyKey()
        );

        task.setTitle(
                request.getTitle()
        );

        task.setDescription(
                request.getDescription()
        );

        task.setPriority(
                request.getPriority()
        );

        task.setRequiredRole(
                request.getRequiredRole()
        );

        task.setAssigneeId(
                request.getAssigneeId()
        );

        task.setCreatedBy(
                createdBy
        );

        task.setDueAt(
                request.getDueDate()
        );

        // New tasks always start as TODO
        task.setStatus(
                TaskStatus.TODO
        );

        return taskRepository.save(task);
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================

    public Task updateTask(
            Long tenantId,
            Long taskId,
            UpdateTaskRequest request) {

        Task task = taskRepository
                .findByIdAndTenantId(
                        taskId,
                        tenantId
                )
                .orElseThrow(() ->
                        new RuntimeException("Task not found")
                );

        if (request.getTitle() != null) {
            task.setTitle(
                    request.getTitle()
            );
        }

        if (request.getDescription() != null) {
            task.setDescription(
                    request.getDescription()
            );
        }

        if (request.getPriority() != null) {
            task.setPriority(
                    request.getPriority()
            );
        }

        if (request.getPharmacyKey() != null) {
            task.setPharmacyKey(
                    request.getPharmacyKey()
            );
        }

        if (request.getRequiredRole() != null) {
            task.setRequiredRole(
                    request.getRequiredRole()
            );
        }

        if (request.getDueDate() != null) {
            task.setDueAt(
                    request.getDueDate()
            );
        }

        return taskRepository.save(task);
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

    public Task updateStatus(
            Long tenantId,
            Long taskId,
            TaskStatus newStatus) {

        Task task = taskRepository
                .findByIdAndTenantId(
                        taskId,
                        tenantId
                )
                .orElseThrow(() ->
                        new RuntimeException("Task not found")
                );

        TaskStatus currentStatus =
                task.getStatus();

        if (!isValidTransition(
                currentStatus,
                newStatus)) {

            throw new IllegalArgumentException(
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }

        task.setStatus(
                newStatus
        );

        // Set completion time only when completed
        if (newStatus == TaskStatus.COMPLETED) {

            task.setCompletedAt(
                    LocalDateTime.now()
            );

        } else {

            task.setCompletedAt(
                    null
            );
        }

        return taskRepository.save(task);
    }

    // =========================================================
    // STATUS TRANSITION RULES
    // =========================================================

    private boolean isValidTransition(
            TaskStatus currentStatus,
            TaskStatus newStatus) {

        if (currentStatus == null ||
                newStatus == null) {

            return false;
        }

        // Same status is allowed
        if (currentStatus == newStatus) {
            return true;
        }

        return switch (currentStatus) {

            case TODO ->
                    newStatus == TaskStatus.IN_PROGRESS
                            || newStatus == TaskStatus.BLOCKED
                            || newStatus == TaskStatus.COMPLETED;

            case IN_PROGRESS ->
                    newStatus == TaskStatus.TODO
                            || newStatus == TaskStatus.BLOCKED
                            || newStatus == TaskStatus.COMPLETED;

            case BLOCKED ->
                    newStatus == TaskStatus.TODO
                            || newStatus == TaskStatus.IN_PROGRESS;

            case COMPLETED ->
                    false;
        };
    }
    public Task assignTask(
            Long tenantId,
            Long taskId,
            Long assigneeId) {

        Task task = taskRepository
                .findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found")
                );

        task.setAssigneeId(assigneeId);

        return taskRepository.save(task);
    }
}