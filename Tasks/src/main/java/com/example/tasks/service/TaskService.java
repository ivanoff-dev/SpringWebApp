package com.example.tasks.service;

import com.example.tasks.aspect.annotaion.LogException;
import com.example.tasks.aspect.annotaion.LogExecution;
import com.example.tasks.aspect.annotaion.LogTracking;
import com.example.tasks.dto.TaskDTO;
import com.example.tasks.model.Task;
import com.example.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
//    @Autowired
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Преобразование из Entity в DTO
    public TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setUserId(task.getUserId());
        return dto;
    }

    // Преобразование из DTO в Entity
    public Task toEntity(TaskDTO dto) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setUserId(dto.getUserId());
        return task;
    }

    // Преобразование списка задач
    public List<TaskDTO> toDTOList(List<Task> tasks) {
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @LogExecution
    @LogException
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @LogExecution
    @LogException
    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @LogException
    @LogTracking
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @LogExecution
    @LogException
    public Task updateTask(Long id, Task task) {
        task.setId(id);
        return taskRepository.save(task);
    }

    @LogExecution
    @LogException
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
