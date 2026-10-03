package com.primerx.task.service;

import com.primerx.task.domain.Task;
import com.primerx.task.domain.TaskComment;
import com.primerx.task.dto.AddCommentRequest;
import com.primerx.task.dto.CreateTaskRequest;
import com.primerx.task.dto.UpdateTaskStatusRequest;

import java.util.List;

public interface TaskService {

    Task createTask(CreateTaskRequest request);

    Task getTaskById(Long id);

    List<Task> getAllTasksForTenant();

    Task updateTaskStatus(Long id, UpdateTaskStatusRequest request);

    TaskComment addComment(Long id, AddCommentRequest request);

    List<TaskComment> getTaskComments(Long id);

    void deleteTask(Long id);
}