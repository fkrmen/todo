package com.fab.todo.task;

import com.fab.todo.entity.Todo;
import com.fab.todo.entity.User;
import com.fab.todo.mapper.UserMapper;
import com.fab.todo.service.EmailService;
import com.fab.todo.service.TodoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class RemindTask {

    private final TodoService todoService;
    private final EmailService emailService;
    private final UserMapper userMapper;

    public RemindTask(TodoService todoService, EmailService emailService, UserMapper userMapper) {
        this.todoService = todoService;
        this.emailService = emailService;
        this.userMapper = userMapper;
    }

    @Scheduled(fixedRate = 300000)
    public void scanAndRemind() {
        System.out.println("[提醒任务] 开始扫描待办事项");
        List<Todo> todos = todoService.findTodosToRemind();

        for (Todo todo : todos) {
            User user = userMapper.selectById(todo.getUserId());
            if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
                continue;
            }

            try {
                String deadline = todo.getDeadline() == null
                        ? "未设置"
                        : todo.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                emailService.sendReminder(user.getEmail(), todo.getTitle(), deadline);
                todoService.markReminded(todo.getId());
                System.out.println("[提醒任务] 已发送提醒：" + user.getUsername() + " -> " + todo.getTitle());
            } catch (Exception e) {
                System.err.println("[提醒任务] 发送失败：" + e.getMessage());
            }
        }

        System.out.println("[提醒任务] 扫描完成，本次处理 " + todos.size() + " 条任务");
    }
}
