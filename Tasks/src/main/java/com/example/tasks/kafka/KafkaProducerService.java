package com.example.tasks.kafka;

import com.example.tasks.dto.TaskStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProducerService {

    private final KafkaTemplate<String, TaskStatusMessage> template;

    public void send(Long taskId, String status) {
        TaskStatusMessage message = new TaskStatusMessage(taskId, status);
        try {
            template.sendDefault(taskId.toString(), message);
            template.flush();
            log.info("Producer sent to default topic: taskId={}, status={}", taskId, status);
        } catch (Exception e) {
            log.error("Error sending Task Status: " + e.getMessage(), e);
        }
    }
}