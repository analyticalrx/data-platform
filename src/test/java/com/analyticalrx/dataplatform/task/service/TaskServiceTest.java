package com.analyticalrx.dataplatform.task.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.security.PermissionGuard;
import com.analyticalrx.dataplatform.auth.security.PharmacyAccessGuard;
import com.analyticalrx.dataplatform.pharmacy.repository.PharmacyRepository;
import com.analyticalrx.dataplatform.task.model.TaskPriority;
import com.analyticalrx.dataplatform.task.model.TaskResponse;
import com.analyticalrx.dataplatform.task.model.TaskStatus;
import com.analyticalrx.dataplatform.task.repository.TaskRepository;

import static org.mockito.Mockito.mock;

class TaskServiceTest {
    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskService(taskRepository, mock(PharmacyRepository.class),
                mock(PharmacyAccessGuard.class), new PermissionGuard());
    }

    @Test
    void tenantAdminCannotReadTasks() {
        var admin = user(4, "TENANT_ADMIN", Set.of());
        assertThrows(AccessDeniedException.class, () -> taskService.list(admin, null, null));
    }

    @Test
    void unassignedPharmacistCannotUpdateStatus() {
        var pharmacist = user(6, "PHARMACIST", Set.of("task.update"));
        when(taskRepository.findVisibleById(pharmacist, 10, true))
                .thenReturn(java.util.Optional.of(task(10, 5)));

        assertThrows(AccessDeniedException.class,
                () -> taskService.updateStatus(pharmacist, 10, TaskStatus.IN_PROGRESS));
    }

    @Test
    void assigneeCanUpdateStatus() {
        var technician = user(5, "TECHNICIAN", Set.of("task.update"));
        var todo = task(10, 5);
        var inProgress = new TaskResponse(todo.id(), todo.pharmacyKey(), todo.title(),
                todo.description(), TaskStatus.IN_PROGRESS, todo.priority(), todo.dueAt(),
                todo.assigneeUserId(), todo.createdByUserId(), todo.createdAt(),
                todo.updatedAt(), todo.completedAt());
        when(taskRepository.findVisibleById(technician, 10, true))
                .thenReturn(java.util.Optional.of(todo));
        when(taskRepository.findVisibleById(technician, 10, false))
                .thenReturn(java.util.Optional.of(inProgress));

        taskService.updateStatus(technician, 10, TaskStatus.IN_PROGRESS);

        verify(taskRepository).updateStatus(technician, 10, TaskStatus.IN_PROGRESS);
        verify(taskRepository).recordActivity(technician, 10, "STATUS_CHANGED",
                TaskStatus.TODO, TaskStatus.IN_PROGRESS);
    }

    private PlatformUserDetails user(long id, String role, Set<String> permissions) {
        return new PlatformUserDetails(id, 1, "test@example.test", "Test", "", "active", 1,
                Set.of(role), permissions, Set.of());
    }

    private TaskResponse task(long id, long assigneeId) {
        var now = OffsetDateTime.now();
        return new TaskResponse(id, "PHARM-001", "Test task", null, TaskStatus.TODO,
                TaskPriority.MEDIUM, null, assigneeId, 1, now, now, null);
    }
}
