package com.fab.todo.controller;

import com.fab.todo.dto.LoginDto;
import com.fab.todo.entity.User;
import com.fab.todo.exception.ForbiddenException;
import com.fab.todo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDto dto) {
        try {
            Map<String, Object> result = userService.adminLogin(dto);
            return ResponseEntity.ok(success(result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> listUsers(HttpServletRequest request) {
        checkAdmin(request);
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(success(users));
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        try {
            userService.toggleUserStatus(id);
            return ResponseEntity.ok(success("操作成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        try {
            userService.resetPassword(id);
            return ResponseEntity.ok(success("密码已重置为 123456"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/force-logout")
    public ResponseEntity<Map<String, Object>> forceLogout(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        userService.forceLogout(id);
        return ResponseEntity.ok(success("用户已被强制下线"));
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("只有管理员可以执行该操作");
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
