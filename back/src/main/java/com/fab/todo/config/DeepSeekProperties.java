package com.fab.todo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek API 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {
    private String baseUrl = "https://api.deepseek.com";
    private String apiKey;
    private String model;
    private int timeoutMs = 15000;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank()
                && model != null && !model.isBlank();
    }
}
