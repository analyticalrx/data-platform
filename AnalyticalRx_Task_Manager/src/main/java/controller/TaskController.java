package controller;

import dto.AssignTaskRequest;
import dto.CreateTaskRequest;
import dto.TaskResponse;
import dto.UpdateTaskRequest;
import dto.UpdateTaskStatusRequest;
import entity.Task;
import service.TaskService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // =========================================================
    // CREATE TASK
    // POST /api/v1/tasks
    // =========================================================

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestBody CreateTaskRequest request) {

        // Temporary user ID for development.
        // We will replace this with authenticated user ID.
        Long createdBy = 1L;

        Task task = taskService.createTask(
                request,
                createdBy
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new TaskResponse(task));
    }

    // =========================================================
    // LIST TASKS
    // GET /api/v1/tasks?page=0&size=20
    // =========================================================

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<Task> tasks =
                taskService.getTasks(
                        tenantId,
                        pageable
                );

        Page<TaskResponse> response =
                tasks.map(TaskResponse::new);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // GET SINGLE TASK
    // GET /api/v1/tasks/{id}
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long id,
            @RequestParam Long tenantId) {

        Task task =
                taskService.getTask(
                        tenantId,
                        id
                );

        return ResponseEntity.ok(
                new TaskResponse(task)
        );
    }

    // =========================================================
    // UPDATE TASK
    // PUT /api/v1/tasks/{id}
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestParam Long tenantId,
            @RequestBody UpdateTaskRequest request) {

        Task task =
                taskService.updateTask(
                        tenantId,
                        id,
                        request
                );

        return ResponseEntity.ok(
                new TaskResponse(task)
        );
    }

    // =========================================================
    // UPDATE STATUS
    // PATCH /api/v1/tasks/{id}/status
    // =========================================================

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam Long tenantId,
            @RequestBody UpdateTaskStatusRequest request) {

        Task task =
                taskService.updateStatus(
                        tenantId,
                        id,
                        request.getStatus()
                );

        return ResponseEntity.ok(
                new TaskResponse(task)
        );
    }
    @PatchMapping("/{id}/assignee")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @RequestParam Long tenantId,
            @RequestBody AssignTaskRequest request) {

        Task task = taskService.assignTask(
                tenantId,
                id,
                request.getAssigneeId()
        );

        return ResponseEntity.ok(
                new TaskResponse(task)
        );
    }
}