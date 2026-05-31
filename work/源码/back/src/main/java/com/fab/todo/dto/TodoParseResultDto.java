package com.fab.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一句话待办解析结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoParseResultDto {
    private String title;
    private String level;
    private String deadline;
}
