package com.primerx.task.service;

import com.primerx.task.domain.Task;
import com.primerx.task.domain.TaskComment;
import com.primerx.task.domain.TaskStatus;
import com.primerx.task.dto.AddCommentRequest;
import com.primerx.task.dto.CreateTaskRequest;
import com.primerx.task.dto.UpdateTaskStatusRequest;
import com.primerx.task.exception.InvalidTaskStateException;
import com.primerx.task.exception.ResourceNotFoundException;
import com.primerx.task.repository.TaskCommentRepository;
import com.primerx.task.repository.TaskRepository;
import com.primerx.task.security.UserContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;

    public TaskServiceImpl(TaskRepository taskRepository, TaskCommentRepository commentRepository) {
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public Task createTask(CreateTaskRequest request) {
        UserContext context = UserContext.getContext();

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(request.getPriority());
        task.setDueAt(request.getDueAt());
        task.setAssigneeId(request.getAssigneeId());
        task.setRequiredRole(request.getRequiredRole());
        task.setTenantId(context.getTenantId());
        task.setPharmacyKey(request.getPharmacyKey() != null ? request.getPharmacyKey() : context.getPharmacyKey());
        task.setCreatorId(context.getUserId());
        task.setCreatedAt(OffsetDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());

        return taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        UserContext context = UserContext.getContext();
        return taskRepository.findByIdAndTenantId(id, context.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getAllTasksForTenant() {
        UserContext context = UserContext.getContext();
        return taskRepository.findByTenantId(context.getTenantId(), Pageable.unpaged()).getContent();
    }

    @Override
    public Task updateTaskStatus(Long id, UpdateTaskStatusRequest request) {
        Task task = getTaskById(id);

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new InvalidTaskStateException("Cannot change status of a completed task");
        }

        try {
            TaskStatus newStatus = TaskStatus.valueOf(request.getStatus().toUpperCase());
            task.setStatus(newStatus);
            if (newStatus == TaskStatus.COMPLETED) {
                task.setCompletedAt(OffsetDateTime.now());
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidTaskStateException("Invalid task status: " + request.getStatus());
        }

        task.setUpdatedAt(OffsetDateTime.now());
        return taskRepository.save(task);
    }

    @Override
    public TaskComment addComment(Long id, AddCommentRequest request) {
        Task task = getTaskById(id);
        UserContext context = UserContext.getContext();

        TaskComment comment = new TaskComment();
        comment.setTaskId(task.getId());
        comment.setTenantId(context.getTenantId());
        comment.setAuthorId(context.getUserId());
        comment.setCommentText(request.getCommentText());
        comment.setCreatedAt(OffsetDateTime.now());
        comment.setUpdatedAt(OffsetDateTime.now());

        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskComment> getTaskComments(Long id) {
        Task task = getTaskById(id);
        UserContext context = UserContext.getContext();
        return commentRepository.findByTaskIdAndTenantIdOrderByCreatedAtDesc(
                task.getId(),
                context.getTenantId(),
                Pageable.unpaged()
        ).getContent();
    }

    @Override
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }
}