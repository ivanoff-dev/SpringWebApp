package com.example.tasks.controller;

import com.example.tasks.model.Task;
import com.example.tasks.repository.TaskRepository;
import com.example.tasks.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("Tasks-test")
@Testcontainers
public class TaskControllerTest {

    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("Интеграционный тест: создание задачи")
    void testCreateTask() throws Exception {
        Task task = new Task();
        task.setTitle("Тестовая задача");
        task.setDescription("Описание тестовой задачи");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Тестовая задача"));
    }

    @Test
    @DisplayName("Интеграционный тест: получение всех задач")
    void testGetAllTasks() throws Exception {
        Task task1 = new Task();
        task1.setTitle("Первая задача");
        task1.setDescription("Описание 1");
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Вторая задача");
        task2.setDescription("Описание 2");
        taskRepository.save(task2);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Интеграционный тест: получение задачи по ID")
    void testGetTaskById() throws Exception {
        Task task = new Task();
        task.setTitle("Найти задачу");
        task.setDescription("Описание поиска");
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(get("/tasks/" + savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Найти задачу"));
    }

    @Test
    @DisplayName("Интеграционный тест: обновление задачи")
    void testUpdateTask() throws Exception {
        Task task = new Task();
        task.setTitle("Старая задача");
        task.setDescription("Старое описание");
        Task savedTask = taskRepository.save(task);

        savedTask.setTitle("Новая задача");
        savedTask.setDescription("Новое описание");

        mockMvc.perform(put("/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Новая задача"));
    }

    @Test
    @DisplayName("Интеграционный тест: удаление задачи")
    void testDeleteTask() throws Exception {
        Task task = new Task();
        task.setTitle("Удаляемая задача");
        task.setDescription("Описание");
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/tasks/" + savedTask.getId()))
                .andExpect(status().isOk());
    }
}
