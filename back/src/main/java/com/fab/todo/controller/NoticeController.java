package com.fab.todo.controller;

import com.fab.todo.entity.Notice;
import com.fab.todo.exception.ForbiddenException;
import com.fab.todo.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> latest() {
        List<Notice> notices = noticeService.getLatestNotices();
        return ResponseEntity.ok(success(notices));
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> all(HttpServletRequest request) {
        checkAdmin(request);
        return ResponseEntity.ok(success(noticeService.getAll()));
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, String> body, HttpServletRequest request) {
        checkAdmin(request);
        try {
            Notice notice = noticeService.create(body.get("title"), body.get("content"));
            return ResponseEntity.ok(success(notice));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        checkAdmin(request);
        try {
            Notice notice = noticeService.update(id, body.get("title"), body.get("content"));
            return ResponseEntity.ok(success(notice));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        noticeService.delete(id);
        return ResponseEntity.ok(success("删除成功"));
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
