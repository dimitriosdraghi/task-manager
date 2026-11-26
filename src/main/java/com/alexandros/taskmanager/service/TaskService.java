package com.alexandros.taskmanager.service;

import com.alexandros.taskmanager.dto.TaskPatchRequest;
import com.alexandros.taskmanager.dto.TaskRequest;
import com.alexandros.taskmanager.dto.TaskResponse;
import com.alexandros.taskmanager.entity.Role;
import com.alexandros.taskmanager.entity.Task;
import com.alexandros.taskmanager.entity.User;
import com.alexandros.taskmanager.exception.TaskNotFoundException;
import com.alexandros.taskmanager.repository.TaskRepository;
import com.alexandros.taskmanager.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks(String username) {
        User currentUser = findUser(username);
        return taskRepository.findByUserIdOrderByIdAsc(currentUser.getId())
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional
    public TaskResponse createTask(String username, TaskRequest request) {
        User currentUser = findUser(username);
        Task task = new Task(request.title().trim(), request.description(), currentUser);
        if (request.completed() != null) {
            task.setCompleted(request.completed());
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id, String username) {
        return TaskResponse.from(findAccessibleTask(id, findUser(username)));
    }

    @Transactional
    public TaskResponse updateTask(Long id, String username, TaskRequest request) {
        Task task = findAccessibleTask(id, findUser(username));
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setCompleted(request.completed() != null && request.completed());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse patchTask(Long id, String username, TaskPatchRequest request) {
        Task task = findAccessibleTask(id, findUser(username));
        if (request.title() != null) {
            if (request.title().isBlank()) {
                throw new IllegalArgumentException("Title must not be blank");
            }
            task.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.completed() != null) {
            task.setCompleted(request.completed());
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id, String username) {
        Task task = findAccessibleTask(id, findUser(username));
        taskRepository.delete(task);
    }

    /** Admin only: access is restricted by the /admin/** rule in SecurityConfig. */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAllByOrderByIdAsc().stream().map(TaskResponse::from).toList();
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /** Ownership rule: a user can access only their own tasks, an ADMIN can access all of them. */
    private Task findAccessibleTask(Long id, User currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        boolean isOwner = task.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only access your own tasks");
        }
        return task;
    }
}
