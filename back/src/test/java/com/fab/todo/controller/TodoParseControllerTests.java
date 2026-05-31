package com.fab.todo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fab.todo.entity.User;
import com.fab.todo.mapper.UserMapper;
import com.fab.todo.service.DeepSeekClient;
import com.fab.todo.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "deepseek.api-key=test-key",
        "deepseek.model=deepseek-chat",
        "deepseek.base-url=https://api.deepseek.com",
        "deepseek.timeout-ms=15000"
})
@AutoConfigureMockMvc
class TodoParseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.fab.todo.config.DeepSeekProperties deepSeekProperties;

    @MockitoBean
    private DeepSeekClient deepSeekClient;

    private String token;

    @BeforeEach
    void setUp() {
        userMapper.delete(new LambdaQueryWrapper<User>().ne(User::getId, -1L));

        User user = new User();
        user.setUsername("parse-user");
        user.setPassword("encoded-password");
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        deepSeekProperties.setApiKey("test-key");
        deepSeekProperties.setModel("deepseek-chat");
    }

    @Test
    void shouldReturnParsedDraftForLoggedInUser() throws Exception {
        Mockito.when(deepSeekClient.chat(Mockito.anyString()))
                .thenReturn("{\"title\":\"提交周报\",\"level\":\"HIGH\",\"deadline\":\"2026-06-01T15:00:00\"}");

        mockMvc.perform(post("/api/todo/parse")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"明天下午3点提交周报，高优先级\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("提交周报"))
                .andExpect(jsonPath("$.data.level").value("HIGH"))
                .andExpect(jsonPath("$.data.deadline").value("2026-06-01T15:00:00"));
    }

    @Test
    void shouldRejectUnauthenticatedParseRequest() throws Exception {
        mockMvc.perform(post("/api/todo/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"记得买牛奶\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn503WhenConfigMissing() throws Exception {
        deepSeekProperties.setApiKey("");

        mockMvc.perform(post("/api/todo/parse")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"记得买牛奶\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(503));
    }
}
