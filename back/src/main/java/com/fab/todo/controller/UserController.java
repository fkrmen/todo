package com.fab.todo.controller;

import com.fab.todo.dto.LoginDto;
import com.fab.todo.dto.PasswordDto;
import com.fab.todo.dto.RegisterDto;
import com.fab.todo.entity.User;
import com.fab.todo.service.NoticeService;
import com.fab.todo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final NoticeService noticeService;

    public UserController(UserService userService, NoticeService noticeService) {
        this.userService = userService;
        this.noticeService = noticeService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterDto dto) {
        try {
            Map<String, Object> result = userService.register(dto);
            return ResponseEntity.ok(success(result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDto dto) {
        try {
            Map<String, Object> result = userService.login(dto);
            return ResponseEntity.ok(success(result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getCurrentUser(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("status", user.getStatus());
        data.put("qq", user.getQq());
        data.put("nickname", user.getNickname());
        data.put("emailChangeCount", user.getEmailChangeCount());
        data.put("emailChangeDate", user.getEmailChangeDate() != null ? user.getEmailChangeDate().toString() : null);
        data.put("lastNoticeSeenAt", user.getLastNoticeSeenAt() != null ? user.getLastNoticeSeenAt().toString() : null);
        return ResponseEntity.ok(success(data));
    }

    @PutMapping("/email")
    public ResponseEntity<Map<String, Object>> updateEmail(HttpServletRequest request, @RequestBody Map<String, String> body) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userService.updateEmail(userId, body.get("email"));
            return ResponseEntity.ok(success("邮箱更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> updatePassword(HttpServletRequest request, @RequestBody PasswordDto dto) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userService.updatePassword(userId, dto);
            return ResponseEntity.ok(success("密码修改成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(HttpServletRequest request, @RequestBody Map<String, String> body) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            User user = userService.updateProfile(userId, body);
            Map<String, Object> data = new HashMap<>();
            data.put("qq", user.getQq());
            data.put("nickname", user.getNickname());
            return ResponseEntity.ok(success(data));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PutMapping("/notice-seen")
    public ResponseEntity<Map<String, Object>> markNoticeSeen(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.markNoticeSeen(userId);
        return ResponseEntity.ok(success("ok"));
    }

    @GetMapping("/has-unread")
    public ResponseEntity<Map<String, Object>> hasUnread(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getCurrentUser(userId);
        boolean hasUnread = noticeService.hasUnreadNotices(user.getLastNoticeSeenAt());
        Map<String, Object> data = new HashMap<>();
        data.put("hasUnread", hasUnread);
        return ResponseEntity.ok(success(data));
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
