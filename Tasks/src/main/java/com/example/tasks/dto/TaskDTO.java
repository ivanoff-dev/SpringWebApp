package com.example.tasks.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Long userId;
    private String status;
}