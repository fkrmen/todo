package com.fab.todo.dto;

import lombok.Data;

/**
 * 待办事项请求 DTO
 */
@Data
public class TodoDto {
    private String title;
    private String level;      // LOW / MEDIUM / HIGH
    private String deadline;   // ISO datetime string, optional
}
