package com.example.tasks.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class TaskStatusMessage {
    // геттеры и сеттеры
    private Long taskId;
    private String status;

    public TaskStatusMessage(Long taskId, String status) {
        this.taskId = taskId;
        this.status = status;
    }

}