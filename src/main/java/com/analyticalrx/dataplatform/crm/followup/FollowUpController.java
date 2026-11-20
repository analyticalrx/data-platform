package com.analyticalrx.dataplatform.crm.followup;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/follow-ups")
public class FollowUpController {

    private final FollowUpTaskService taskService;

    @Autowired
    public FollowUpController(FollowUpTaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<FollowUpTaskResponseDTO> createTask(@Valid @RequestBody FollowUpTaskRequestDTO requestDTO) {
        return new ResponseEntity<>(taskService.createTask(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FollowUpTaskResponseDTO>> getTasks(
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false) UUID officeId) {
        return ResponseEntity.ok(taskService.getTasks(assignedTo, status, overdue, officeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FollowUpTaskResponseDTO> getTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FollowUpTaskResponseDTO> updateTask(
            @PathVariable UUID id,
            @RequestBody FollowUpTaskPatchDTO patchDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, patchDTO));
    }
}