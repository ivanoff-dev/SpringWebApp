package com.example.tasks.service;

import com.example.tasks.dto.TaskStatusMessage;
import com.example.tasks.model.Task;
import com.example.tasks.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("Tasks-test")
class TaskServiceTest {

    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        doNothing().when(notificationService).sendStatusChangedNotification(anyList());
    }

    @Test
    @DisplayName("Тест №1 - Создание задачи")
    void testCreateTask() {
        Task task = new Task();
        task.setTitle("Задача №1");
        task.setDescription("Описание задачи №1");
        task.setStatus("START");

        Task createdTask = taskService.createTask(task);

        assertNotNull(createdTask.getId(), "ID созданной задачи должен быть не null");
        assertEquals("Задача №1", createdTask.getTitle());
        assertEquals("Описание задачи №1", createdTask.getDescription());
    }

    @Test
    @DisplayName("Тест №2 - Получение задачи по ID")
    void testGetTaskById() {
        Task task = new Task();
        task.setTitle("Задача №1");
        task.setDescription("Описание задачи №1");
        task.setStatus("START");

        Task savedTask = taskRepository.save(task);

        Task foundTask = taskService.getTaskById(savedTask.getId());

        assertNotNull(foundTask, "Задача должна быть найдена");
        assertEquals(savedTask.getTitle(), foundTask.getTitle());
        assertEquals(savedTask.getDescription(), foundTask.getDescription());
    }

    @Test
    @DisplayName("Тест №3 - Получение всех задач")
    void testGetAllTasks() {
        Task task1 = new Task();
        task1.setTitle("Задача №1");
        task1.setDescription("Описание задачи №1");
        task1.setStatus("START");

        Task task2 = new Task();
        task2.setTitle("Задача №2");
        task2.setDescription("Описание задачи №2");
        task2.setStatus("START");

        taskRepository.save(task1);
        taskRepository.save(task2);

        List<Task> allTasks = taskService.getAllTasks();

        assertEquals(2, allTasks.size(), "Должно быть 2 задачи");
    }

    @Test
    @DisplayName("Тест №4 - Обновление задачи")
    void testUpdateTask() {
        Task task = new Task();
        task.setTitle("Задача №1");
        task.setDescription("Описание задачи №1");
        task.setStatus("START");

        Task savedTask = taskRepository.save(task);

        savedTask.setTitle("Задача №1");
        savedTask.setDescription("Новое описание задачи №1");
        savedTask.setStatus("IN_PROGRESS");

        Task updatedTask = taskService.updateTask(savedTask.getId(), savedTask);

        assertEquals("Задача №1", updatedTask.getTitle());
        assertEquals("Новое описание задачи №1", updatedTask.getDescription());
        assertEquals("IN_PROGRESS", updatedTask.getStatus());
    }

    @Test
    @DisplayName("Тест №5 - Удаление задачи")
    void testDeleteTask() {
        Task task = new Task();
        task.setTitle("Задача №1");
        task.setDescription("Описание задачи №1");

        Task savedTask = taskRepository.save(task);

        taskService.deleteTask(savedTask.getId());

        Optional<Task> deletedTask = taskRepository.findById(savedTask.getId());
        assertFalse(deletedTask.isPresent(), "Задача должна быть удалена");
    }

    @Test
    @DisplayName("Тест №6 - Получение задачи по несуществующему ID")
    void testGetTaskById_NotFound() {
        Long badId = 9999L;

        Exception exception = assertThrows(
                RuntimeException.class,
                () -> taskService.getTaskById(badId),
                "Исключение при отсутствии задачи"
        );

    }

    @Test
    @DisplayName("Тест №7 - Обновление задачи по несуществующему ID")
    void testUpdateTask_NotFound() {
        Long badId = 9999L;
        Task task = new Task();

        Exception exception = assertThrows(
                RuntimeException.class,
                () -> taskService.updateTask(badId, task),
                "Исключение при отсутствии задачи"
        );

    }

    @Test
    @DisplayName("Тест №8 - Удаление задачи по несуществующему ID")
    void testDeleteTask_NotFound() {
        Long badId = 9999L;

        Exception exception = assertThrows(
                RuntimeException.class,
                () -> taskService.deleteTask(badId),
                "Исключение при отсутствии задачи"
        );

    }
}