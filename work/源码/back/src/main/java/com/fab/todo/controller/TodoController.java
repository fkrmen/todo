package com.fab.todo.controller;

import com.fab.todo.dto.TodoDto;
import com.fab.todo.entity.Todo;
import com.fab.todo.service.TodoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 待办事项控制器
 * 普通用户 CRUD 自己的待办事项
 */
@RestController
@RequestMapping("/api/todo")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /** 获取当前用户的所有待办，未登录返回空列表 */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(success(java.util.Collections.emptyList()));
        }
        List<Todo> list = todoService.getUserTodos(userId);
        return ResponseEntity.ok(success(list));
    }

    /** 获取单条待办 */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Todo todo = todoService.getById(id, userId);
            return ResponseEntity.ok(success(todo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    /** 新增待办 */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody TodoDto dto, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Todo todo = todoService.create(dto, userId);
            return ResponseEntity.ok(success(todo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    /** 更新待办 */
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                       @RequestBody TodoDto dto,
                                                       HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Todo todo = todoService.update(id, dto, userId);
            return ResponseEntity.ok(success(todo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    /** 切换完成状态 */
    @PutMapping("/toggle/{id}")
    public ResponseEntity<Map<String, Object>> toggle(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Todo todo = todoService.toggleDone(id, userId);
            return ResponseEntity.ok(success(todo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    /** 删除待办 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            todoService.delete(id, userId);
            return ResponseEntity.ok(success("删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "success");
        map.put("data", data);
        return map;
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 400);
        map.put("msg", msg);
        return map;
    }
}
