package com.alexandros.taskmanager.dto;

import jakarta.validation.constraints.Size;

/** Used for PATCH: only the non-null fields are updated. */
public record TaskPatchRequest(
        @Size(max = 100, message = "Title must be at most 100 characters") String title,
        @Size(max = 500, message = "Description must be at most 500 characters") String description,
        Boolean completed
) {
}
