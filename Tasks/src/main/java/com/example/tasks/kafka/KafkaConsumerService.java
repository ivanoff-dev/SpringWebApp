package com.example.tasks.kafka;

import com.example.tasks.dto.TaskDTO;
import com.example.tasks.dto.TaskStatusMessage;
import com.example.tasks.model.Task;
import com.example.tasks.service.NotificationService;
import com.example.tasks.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class KafkaConsumerService {

    private final NotificationService notificationService;

    public KafkaConsumerService(NotificationService notificationService, TaskService taskService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(groupId = "${kafka.consumer.group-id}",
                   topics = "tasks_status_changing",
                   containerFactory = "kafkaListenerContainerFactory")
    public void listenTaskStatusChange(@Payload TaskStatusMessage message,
                                       Acknowledgment ack) {
        log.debug("Task consumer: Обработка новых сообщений");
        try {
            notificationService.sendStatusChangedNotification(List.of(message));
        } finally {
            ack.acknowledge();
        }
        log.debug("Client consumer: записи обработаны");
    }
}
