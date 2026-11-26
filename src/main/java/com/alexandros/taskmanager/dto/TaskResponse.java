package com.alexandros.taskmanager.dto;

import com.alexandros.taskmanager.entity.Task;

import java.time.Instant;

public record TaskResponse(Long id, String title, String description, boolean completed,
                           Instant createdAt, String owner) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUser().getUsername());
    }
}
