package com.example.tasks.service;

import com.example.tasks.aspect.annotaion.LogExecution;
import com.example.tasks.aspect.annotaion.LogTracking;
import com.example.tasks.dto.TaskDTO;
import com.example.tasks.dto.TaskStatusMessage;
import com.example.tasks.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${notification.mail.to}")
    private String toEmail;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendStatusChangedNotification(List<TaskStatusMessage> messages) {
        try {
            StringBuilder messageBuilder = new StringBuilder("Изменения в задачах:\n\n");
            System.out.println("Зашли в sendStatusChangedNotification");
            for (TaskStatusMessage msg : messages) {
                messageBuilder.append(String.format("Задача ID: %d, Новый статус: %s%n",
                        msg.getTaskId(), msg.getStatus()));
            }

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(toEmail);
            System.out.println("Зашли в sendStatusChangedNotification");
            mailMessage.setFrom(((JavaMailSenderImpl) mailSender).getUsername());
            mailMessage.setSubject("Изменения статуса задач");
            mailMessage.setText(messageBuilder.toString());

            mailSender.send(mailMessage);
            System.out.println("Письмо отправлено со списком задач.");
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", e.getMessage(), e);
        }
    }
}
