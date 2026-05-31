package com.fab.todo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fab.todo.config.DeepSeekProperties;
import com.fab.todo.dto.TodoParseResultDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class TodoParseService {

    private static final String DEFAULT_LEVEL = "MEDIUM";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    private final DeepSeekProperties properties;
    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper;

    public TodoParseService(DeepSeekProperties properties,
                            DeepSeekClient deepSeekClient,
                            ObjectMapper objectMapper) {
        this.properties = properties;
        this.deepSeekClient = deepSeekClient;
        this.objectMapper = objectMapper;
    }

    public TodoParseResultDto parse(String text) {
        String normalizedText = normalizeInput(text);
        if (normalizedText.isEmpty()) {
            throw new RuntimeException("待办内容不能为空");
        }
        if (!properties.isConfigured()) {
            throw new IllegalStateException("DeepSeek 配置不完整");
        }

        try {
            String prompt = buildPrompt(normalizedText);
            String responseText = deepSeekClient.chat(prompt);
            JsonNode root = objectMapper.readTree(extractJsonObject(responseText));
            return normalizeResult(root, normalizedText);
        } catch (Exception e) {
            return fallback(normalizedText);
        }
    }

    TodoParseResultDto normalizeResult(JsonNode root, String originalText) {
        String title = normalizeTitle(root.path("title").asText(null), originalText);
        String level = normalizeLevel(root.path("level").asText(null));
        String deadline = normalizeDeadline(root.path("deadline").asText(null));
        return new TodoParseResultDto(title, level, deadline);
    }

    TodoParseResultDto fallback(String text) {
        return new TodoParseResultDto(text, DEFAULT_LEVEL, null);
    }

    String extractJsonObject(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("DeepSeek response is empty");
        }

        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("DeepSeek response does not contain JSON");
        }
        return trimmed.substring(start, end + 1);
    }

    private String buildPrompt(String text) {
        String now = LocalDateTime.now(SHANGHAI_ZONE).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return """
                你是待办事项解析助手。请从用户输入中提取一条待办事项，并且只返回 JSON，不要返回解释、Markdown 或代码块。

                当前时区：Asia/Shanghai
                当前时间：%s

                返回 JSON 格式固定为：
                {"title":"待办标题","level":"LOW|MEDIUM|HIGH","deadline":"YYYY-MM-DDTHH:mm:ss 或 null"}

                规则：
                1. title 必须是适合展示的简洁待办标题。
                2. level 只能是 LOW、MEDIUM、HIGH 之一；无法判断时返回 MEDIUM。
                3. deadline 必须是 ISO 本地时间格式 YYYY-MM-DDTHH:mm:ss；无法判断时返回 null。
                4. 如果输入里有“高优先级/紧急/马上”等，优先返回 HIGH；“低优先级/不着急”等可返回 LOW。
                5. 如果只有日期没有具体时分，尽量结合语义补足合理时间；没有依据时返回 null。

                用户输入：
                %s
                """.formatted(now, text);
    }

    private String normalizeInput(String text) {
        return text == null ? "" : text.trim();
    }

    private String normalizeTitle(String title, String fallbackText) {
        if (title == null || title.isBlank()) {
            return fallbackText;
        }
        return title.trim();
    }

    private String normalizeLevel(String level) {
        if (level == null) {
            return DEFAULT_LEVEL;
        }

        String normalized = level.trim().toUpperCase();
        return switch (normalized) {
            case "LOW", "MEDIUM", "HIGH" -> normalized;
            default -> DEFAULT_LEVEL;
        };
    }

    private String normalizeDeadline(String deadline) {
        if (deadline == null) {
            return null;
        }

        String normalized = deadline.trim();
        if (normalized.isEmpty() || "null".equalsIgnoreCase(normalized)) {
            return null;
        }

        try {
            LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return normalized;
        } catch (Exception e) {
            return null;
        }
    }
}
