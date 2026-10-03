package com.analyticalrx.dataplatform.task.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.task.model.AddTaskCommentRequest;
import com.analyticalrx.dataplatform.task.model.CreateTaskRequest;
import com.analyticalrx.dataplatform.task.model.TaskCommentResponse;
import com.analyticalrx.dataplatform.task.model.TaskResponse;
import com.analyticalrx.dataplatform.task.model.TaskStatus;
import com.analyticalrx.dataplatform.task.model.UpdateTaskStatusRequest;
import com.analyticalrx.dataplatform.task.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal PlatformUserDetails user,
                                               @Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(user, request));
    }

    @GetMapping
    public List<TaskResponse> list(@AuthenticationPrincipal PlatformUserDetails user,
                                   @RequestParam(required = false) String pharmacyKey,
                                   @RequestParam(required = false) TaskStatus status) {
        return taskService.list(user, pharmacyKey, status);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@AuthenticationPrincipal PlatformUserDetails user, @PathVariable long id) {
        return taskService.get(user, id);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(@AuthenticationPrincipal PlatformUserDetails user, @PathVariable long id,
                                     @Valid @RequestBody UpdateTaskStatusRequest request) {
        return taskService.updateStatus(user, id, request.status());
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<TaskCommentResponse> addComment(@AuthenticationPrincipal PlatformUserDetails user,
                                                          @PathVariable long id,
                                                          @Valid @RequestBody AddTaskCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addComment(user, id, request));
    }

    @GetMapping("/{id}/comments")
    public List<TaskCommentResponse> listComments(@AuthenticationPrincipal PlatformUserDetails user,
                                                  @PathVariable long id) {
        return taskService.listComments(user, id);
    }
}
