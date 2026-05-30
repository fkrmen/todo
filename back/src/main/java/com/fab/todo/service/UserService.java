package com.fab.todo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fab.todo.dto.LoginDto;
import com.fab.todo.dto.PasswordDto;
import com.fab.todo.dto.RegisterDto;
import com.fab.todo.entity.User;
import com.fab.todo.mapper.UserMapper;
import com.fab.todo.utils.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void initAdmin() {
        long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
        if (count > 0) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(encoder.encode("admin123"));
        admin.setRole("ADMIN");
        admin.setStatus(1);
        userMapper.insert(admin);
        System.out.println("[init] default admin created: admin / admin123");
    }

    public Map<String, Object> register(RegisterDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new RuntimeException("密码至少 6 位");
        }

        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        return buildAuthResult(user);
    }

    public Map<String, Object> login(LoginDto dto) {
        User user = findByUsername(dto.getUsername());
        if (user == null || !encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
        return buildAuthResult(user);
    }

    public Map<String, Object> adminLogin(LoginDto dto) {
        User user = findByUsername(dto.getUsername());
        if (user == null || !"ADMIN".equals(user.getRole()) || !encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("管理员账号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("管理员账号已被禁用");
        }
        return buildAuthResult(user);
    }

    public User getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        LocalDate today = LocalDate.now();
        if (user.getEmailChangeDate() == null || !user.getEmailChangeDate().equals(today)) {
            user.setEmailChangeCount(0);
        }
        return user;
    }

    public void updateEmail(Long userId, String email) {
        String normalizedEmail = email == null ? null : email.trim();
        if (normalizedEmail == null || !normalizedEmail.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            throw new RuntimeException("邮箱格式不正确");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (normalizedEmail.equals(user.getEmail())) {
            return;
        }

        LocalDate today = LocalDate.now();
        if (user.getEmailChangeDate() == null || !user.getEmailChangeDate().equals(today)) {
            user.setEmailChangeCount(0);
            user.setEmailChangeDate(today);
        }

        if (user.getEmailChangeCount() != null && user.getEmailChangeCount() >= 3) {
            throw new RuntimeException("今日邮箱修改次数已达上限，请明天再试");
        }

        user.setEmail(normalizedEmail);
        user.setEmailChangeCount(user.getEmailChangeCount() == null ? 1 : user.getEmailChangeCount() + 1);
        userMapper.updateById(user);
    }

    public User updateProfile(Long userId, Map<String, String> body) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (body.containsKey("qq")) {
            String qq = body.get("qq");
            user.setQq(qq == null ? null : qq.trim());
        }

        if (body.containsKey("nickname")) {
            String nickname = body.get("nickname");
            String normalizedNickname = nickname == null ? "" : nickname.trim();
            if (normalizedNickname.isEmpty()) {
                throw new RuntimeException("昵称不能为空");
            }
            user.setNickname(normalizedNickname);
        }

        userMapper.updateById(user);
        return user;
    }

    public void markNoticeSeen(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setLastNoticeSeenAt(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    public void updatePassword(Long userId, PasswordDto dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!encoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new RuntimeException("新密码至少 6 位");
        }

        user.setPassword(encoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    public List<User> getAllUsers() {
        return userMapper.selectList(new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
    }

    public void toggleUserStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
    }

    public void resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(encoder.encode("123456"));
        userMapper.updateById(user);
    }

    public void forceLogout(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setForceLogoutAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    public boolean isForceLogout(Long userId, long tokenIssuedAt) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return true;
        }
        if (user.getForceLogoutAt() == null) {
            return false;
        }
        long forceLogoutAt = user.getForceLogoutAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        return tokenIssuedAt < forceLogoutAt;
    }

    private User findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username.trim()));
    }

    private Map<String, Object> buildAuthResult(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("qq", user.getQq());
        result.put("nickname", user.getNickname());
        return result;
    }
}
