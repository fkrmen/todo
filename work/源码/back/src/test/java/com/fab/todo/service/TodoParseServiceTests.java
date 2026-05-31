package com.fab.todo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fab.todo.config.DeepSeekProperties;
import com.fab.todo.dto.TodoParseResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TodoParseServiceTests {

    private DeepSeekProperties properties;
    private DeepSeekClient deepSeekClient;
    private TodoParseService todoParseService;

    @BeforeEach
    void setUp() {
        properties = new DeepSeekProperties();
        properties.setApiKey("test-key");
        properties.setModel("deepseek-chat");
        deepSeekClient = mock(DeepSeekClient.class);
        todoParseService = new TodoParseService(properties, deepSeekClient, new ObjectMapper());
    }

    @Test
    void shouldParseCompleteJson() throws Exception {
        when(deepSeekClient.chat(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("{\"title\":\"提交周报\",\"level\":\"HIGH\",\"deadline\":\"2026-06-01T15:00:00\"}");

        TodoParseResultDto result = todoParseService.parse("明天下午3点提交周报，高优先级");

        assertEquals("提交周报", result.getTitle());
        assertEquals("HIGH", result.getLevel());
        assertEquals("2026-06-01T15:00:00", result.getDeadline());
    }

    @Test
    void shouldDefaultLevelWhenMissing() throws Exception {
        when(deepSeekClient.chat(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("{\"title\":\"买牛奶\",\"deadline\":null}");

        TodoParseResultDto result = todoParseService.parse("记得买牛奶");

        assertEquals("买牛奶", result.getTitle());
        assertEquals("MEDIUM", result.getLevel());
        assertNull(result.getDeadline());
    }

    @Test
    void shouldDropInvalidDeadline() throws Exception {
        when(deepSeekClient.chat(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("{\"title\":\"提交周报\",\"level\":\"HIGH\",\"deadline\":\"tomorrow\"}");

        TodoParseResultDto result = todoParseService.parse("明天下午3点提交周报，高优先级");

        assertEquals("提交周报", result.getTitle());
        assertEquals("HIGH", result.getLevel());
        assertNull(result.getDeadline());
    }

    @Test
    void shouldFallbackToOriginalTextWhenTitleEmpty() throws Exception {
        when(deepSeekClient.chat(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("{\"title\":\"\",\"level\":\"LOW\",\"deadline\":null}");

        TodoParseResultDto result = todoParseService.parse("记得买牛奶");

        assertEquals("记得买牛奶", result.getTitle());
        assertEquals("LOW", result.getLevel());
        assertNull(result.getDeadline());
    }

    @Test
    void shouldFallbackWhenModelResponseIsInvalid() throws Exception {
        when(deepSeekClient.chat(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("not-json");

        TodoParseResultDto result = todoParseService.parse("记得买牛奶");

        assertEquals("记得买牛奶", result.getTitle());
        assertEquals("MEDIUM", result.getLevel());
        assertNull(result.getDeadline());
    }

    @Test
    void shouldThrowWhenConfigMissing() {
        properties.setApiKey("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> todoParseService.parse("记得买牛奶"));

        assertEquals("DeepSeek 配置不完整", exception.getMessage());
    }
}
