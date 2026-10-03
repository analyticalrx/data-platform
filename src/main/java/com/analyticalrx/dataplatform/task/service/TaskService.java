package com.analyticalrx.dataplatform.task.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.security.PermissionGuard;
import com.analyticalrx.dataplatform.auth.security.PharmacyAccessGuard;
import com.analyticalrx.dataplatform.pharmacy.repository.PharmacyRepository;
import com.analyticalrx.dataplatform.task.model.AddTaskCommentRequest;
import com.analyticalrx.dataplatform.task.model.CreateTaskRequest;
import com.analyticalrx.dataplatform.task.model.TaskCommentResponse;
import com.analyticalrx.dataplatform.task.model.TaskResponse;
import com.analyticalrx.dataplatform.task.model.TaskStatus;
import com.analyticalrx.dataplatform.task.repository.TaskRepository;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyAccessGuard pharmacyAccessGuard;
    private final PermissionGuard permissionGuard;

    public TaskService(TaskRepository taskRepository, PharmacyRepository pharmacyRepository,
                       PharmacyAccessGuard pharmacyAccessGuard, PermissionGuard permissionGuard) {
        this.taskRepository = taskRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.pharmacyAccessGuard = pharmacyAccessGuard;
        this.permissionGuard = permissionGuard;
    }

    @Transactional
    public TaskResponse create(PlatformUserDetails user, CreateTaskRequest request) {
        permissionGuard.require(user, "task.create");
        pharmacyAccessGuard.requirePharmacyAccess(user, request.pharmacyKey());
        pharmacyRepository.findActiveByKey(user, request.pharmacyKey())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found"));
        if (!taskRepository.canAssignTo(request.assigneeUserId(), user.tenantId(), request.pharmacyKey())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee cannot access this pharmacy");
        }

        long taskId = taskRepository.create(user, request);
        taskRepository.recordActivity(user, taskId, "CREATED", null, TaskStatus.TODO);
        return findVisible(user, taskId, false);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(PlatformUserDetails user, String pharmacyKey, TaskStatus status) {
        permissionGuard.require(user, "task.read");
        if (pharmacyKey != null) {
            pharmacyAccessGuard.requirePharmacyAccess(user, pharmacyKey);
        }
        return taskRepository.listVisible(user, pharmacyKey, status);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(PlatformUserDetails user, long taskId) {
        permissionGuard.require(user, "task.read");
        return findVisible(user, taskId, false);
    }

    @Transactional
    public TaskResponse updateStatus(PlatformUserDetails user, long taskId, TaskStatus newStatus) {
        permissionGuard.require(user, "task.update");
        TaskResponse task = findVisible(user, taskId, true);
        boolean manager = user.roles().contains("OWNER") || user.roles().contains("STORE_MANAGER");
        if (!manager && task.assigneeUserId() != user.id()) {
            throw new AccessDeniedException("Only the assignee or manager can update this task");
        }
        if (task.status() == TaskStatus.COMPLETED || task.status() == TaskStatus.SKIPPED
                || task.status() == TaskStatus.CANCELED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task is already closed");
        }
        if (task.status() != newStatus) {
            taskRepository.updateStatus(user, taskId, newStatus);
            taskRepository.recordActivity(user, taskId, "STATUS_CHANGED", task.status(), newStatus);
        }
        return findVisible(user, taskId, false);
    }

    @Transactional
    public TaskCommentResponse addComment(PlatformUserDetails user, long taskId, AddTaskCommentRequest request) {
        permissionGuard.require(user, "task.comment");
        findVisible(user, taskId, false);
        TaskCommentResponse comment = taskRepository.addComment(user, taskId, request.commentText());
        taskRepository.recordActivity(user, taskId, "COMMENT_ADDED", null, null);
        return comment;
    }

    @Transactional(readOnly = true)
    public List<TaskCommentResponse> listComments(PlatformUserDetails user, long taskId) {
        permissionGuard.require(user, "task.read");
        findVisible(user, taskId, false);
        return taskRepository.listComments(user, taskId);
    }

    private TaskResponse findVisible(PlatformUserDetails user, long taskId, boolean lock) {
        return taskRepository.findVisibleById(user, taskId, lock)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }
}
