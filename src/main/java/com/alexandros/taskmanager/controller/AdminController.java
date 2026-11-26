package com.alexandros.taskmanager.controller;

import com.alexandros.taskmanager.dto.TaskResponse;
import com.alexandros.taskmanager.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final TaskService taskService;

    public AdminController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public List<TaskResponse> getAllTasks() {
        return taskService.getAllTasks();
    }
}
