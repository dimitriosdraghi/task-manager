package com.alexandros.taskmanager.controller;

import com.alexandros.taskmanager.dto.TaskPatchRequest;
import com.alexandros.taskmanager.dto.TaskRequest;
import com.alexandros.taskmanager.dto.TaskResponse;
import com.alexandros.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** The current user is taken from the JWT (authentication.getName() returns the username). */
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> getMyTasks(Authentication authentication) {
        return taskService.getMyTasks(authentication.getName());
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request,
                                                   Authentication authentication) {
        TaskResponse created = taskService.createTask(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable("id") Long id, Authentication authentication) {
        return taskService.getTask(id, authentication.getName());
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable("id") Long id, @Valid @RequestBody TaskRequest request,
                                   Authentication authentication) {
        return taskService.updateTask(id, authentication.getName(), request);
    }

    @PatchMapping("/{id}")
    public TaskResponse patchTask(@PathVariable("id") Long id, @Valid @RequestBody TaskPatchRequest request,
                                  Authentication authentication) {
        return taskService.patchTask(id, authentication.getName(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id, Authentication authentication) {
        taskService.deleteTask(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
