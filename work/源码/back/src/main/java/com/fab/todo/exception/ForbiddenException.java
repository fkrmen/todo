package com.fab.todo.exception;

/**
 * 无权限访问（返回 HTTP 403）
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
