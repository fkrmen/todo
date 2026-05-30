package com.fab.todo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待办事项实体
 */
@Data
@TableName("fab_todo")
public class Todo {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 待办标题 */
    private String title;

    /** 级别: LOW / MEDIUM / HIGH */
    private String level;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 是否完成: 0=未完成, 1=已完成 */
    private Integer done;

    /** 是否已邮件提醒: 0=否, 1=是 */
    private Integer reminded;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
