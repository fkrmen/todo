package com.fab.todo.config;

import com.fab.todo.service.UserService;
import com.fab.todo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Set<String> WHITE_LIST = Set.of(
            "/api/user/login",
            "/api/user/register",
            "/api/admin/login"
    );

    private static final Set<String> OPTIONAL_AUTH = Set.of(
            "/api/todo/list",
            "/api/notice/latest"
    );

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtInterceptor(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        if (WHITE_LIST.contains(path)) {
            tryApplyToken(request, authHeader, false);
            return true;
        }

        if (OPTIONAL_AUTH.contains(path)) {
            tryApplyToken(request, authHeader, false);
            return true;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(response, "未登录或登录状态无效");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return unauthorized(response, "登录已过期，请重新登录");
        }

        if (!tryApplyToken(request, authHeader, true)) {
            return unauthorized(response, "账号已被强制下线，请重新登录");
        }

        return true;
    }

    private boolean tryApplyToken(HttpServletRequest request, String authHeader, boolean rejectForceLogout) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        Long userId = jwtUtil.getUserId(token);
        long issuedAt = jwtUtil.getIssuedAt(token);
        if (userService.isForceLogout(userId, issuedAt)) {
            return !rejectForceLogout;
        }

        request.setAttribute("userId", userId);
        request.setAttribute("username", jwtUtil.getUsername(token));
        request.setAttribute("role", jwtUtil.getRole(token));
        return true;
    }

    private boolean unauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"" + msg + "\"}");
        return false;
    }
}
