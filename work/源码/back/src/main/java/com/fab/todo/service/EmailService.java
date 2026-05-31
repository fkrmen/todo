package com.fab.todo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReminder(String to, String title, String deadline) {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("未配置发件邮箱");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("【待办提醒】你有一条任务即将到期");
        message.setText(
                "你好，\n\n"
                        + "你有以下待办事项即将到期：\n"
                        + "标题：" + title + "\n"
                        + "截止时间：" + deadline + "\n\n"
                        + "请尽快处理。\n\n"
                        + "Todo 待办系统自动提醒\n\n"
                        + "By Fkrmen"
        );
        mailSender.send(message);
    }
}
