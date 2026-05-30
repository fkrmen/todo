package com.fab.todo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fab.todo.dto.TodoDto;
import com.fab.todo.entity.Todo;
import com.fab.todo.mapper.TodoMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> getUserTodos(Long userId) {
        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<Todo>()
                .eq(Todo::getUserId, userId)
                .orderByAsc(Todo::getDone)
                .orderByDesc(Todo::getCreatedAt);
        return todoMapper.selectList(wrapper);
    }

    public Todo getById(Long id, Long userId) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在");
        }
        return todo;
    }

    public Todo create(TodoDto dto, Long userId) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new RuntimeException("任务标题不能为空");
        }

        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setTitle(dto.getTitle().trim());
        todo.setLevel(dto.getLevel() != null ? dto.getLevel() : "MEDIUM");
        todo.setDone(0);
        todo.setReminded(0);

        if (dto.getDeadline() != null && !dto.getDeadline().isEmpty()) {
            todo.setDeadline(LocalDateTime.parse(dto.getDeadline(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        todoMapper.insert(todo);
        return todo;
    }

    public Todo update(Long id, TodoDto dto, Long userId) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new RuntimeException("任务标题不能为空");
        }

        todo.setTitle(dto.getTitle().trim());
        todo.setLevel(dto.getLevel() != null ? dto.getLevel() : todo.getLevel());

        if (dto.getDeadline() != null && !dto.getDeadline().isEmpty()) {
            todo.setDeadline(LocalDateTime.parse(dto.getDeadline(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            todo.setDeadline(null);
        }

        todo.setReminded(0);
        todoMapper.updateById(todo);
        return todoMapper.selectById(id);
    }

    public Todo toggleDone(Long id, Long userId) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在");
        }
        todo.setDone(todo.getDone() == 1 ? 0 : 1);
        todoMapper.updateById(todo);
        return todo;
    }

    public void delete(Long id, Long userId) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在");
        }
        todoMapper.deleteById(id);
    }

    public List<Todo> findTodosToRemind() {
        LocalDateTime oneHourLater = LocalDateTime.now().plusHours(1);
        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<Todo>()
                .eq(Todo::getDone, 0)
                .eq(Todo::getReminded, 0)
                .isNotNull(Todo::getDeadline)
                .le(Todo::getDeadline, oneHourLater);
        return todoMapper.selectList(wrapper);
    }

    public void markReminded(Long id) {
        Todo todo = todoMapper.selectById(id);
        if (todo != null) {
            todo.setReminded(1);
            todoMapper.updateById(todo);
        }
    }
}
