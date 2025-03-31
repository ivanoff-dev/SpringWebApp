package com.example.tasks.controller;

import com.example.tasks.dto.TaskDTO;
import com.example.tasks.service.TaskService;
import com.example.tasks.model.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public TaskDTO createTask(@RequestBody TaskDTO taskDTO) {
        Task task = taskService.toEntity(taskDTO);
        Task savedTask = taskService.createTask(task); // Предположим, что createTask() сохраняет задачу
        return taskService.toDTO(savedTask);
    }

    @GetMapping("/{id}")
    public TaskDTO getTask(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return taskService.toDTO(task);
    }

    @GetMapping
    public List<TaskDTO> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return taskService.toDTOList(tasks);
    }

    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        Task task = taskService.toEntity(taskDTO);
        Task updatedTask = taskService.updateTask(id, task);
        return taskService.toDTO(updatedTask);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
