package com.fab.todo.dto;

import lombok.Data;

/**
 * 修改密码 DTO
 */
@Data
public class PasswordDto {
    private String oldPassword;
    private String newPassword;
}
