package com.example.tasks.service;

import com.example.starter.aspect.annotation.LogException;
import com.example.starter.aspect.annotation.LogExecution;
import com.example.starter.aspect.annotation.LogTracking;
import com.example.tasks.dto.TaskDTO;
import com.example.tasks.kafka.KafkaProducerService;
import com.example.tasks.model.Task;
import com.example.tasks.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final KafkaProducerService kafkaProducerService;
    private final TaskRepository taskRepository;

    public TaskService(KafkaProducerService kafkaProducerService, TaskRepository taskRepository) {
        this.kafkaProducerService = kafkaProducerService;
        this.taskRepository = taskRepository;
    }

    // Преобразование из Entity в DTO
    public TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setUserId(task.getUserId());
        dto.setStatus(task.getStatus());
        return dto;
    }

    // Преобразование из DTO в Entity
    public Task toEntity(TaskDTO dto) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setUserId(dto.getUserId());
        task.setStatus(dto.getStatus());
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
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));
    }

    @LogException
    @LogTracking
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @LogExecution
    @LogException
    public Task updateTask(Long id, Task updatedTask) {
        Task currentTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));
        boolean statusChanged = (currentTask.getStatus() == null && updatedTask.getStatus() != null)
                || (currentTask.getStatus() != null && !currentTask.getStatus().equals(updatedTask.getStatus()));

        currentTask.setTitle(updatedTask.getTitle());
        currentTask.setDescription(updatedTask.getDescription());
        currentTask.setUserId(updatedTask.getUserId());
        currentTask.setStatus(updatedTask.getStatus());

        if (statusChanged) {
            kafkaProducerService.send(currentTask.getId(), currentTask.getStatus());
        }

        return taskRepository.save(currentTask);
    }

    @LogExecution
    @LogException
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));

        taskRepository.delete(task);
    }
}
