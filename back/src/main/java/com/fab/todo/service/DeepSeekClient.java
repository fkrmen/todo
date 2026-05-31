package com.fab.todo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fab.todo.config.DeepSeekProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.Map;

@Service
public class DeepSeekClient {

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public DeepSeekClient(DeepSeekProperties properties,
                          ObjectMapper objectMapper,
                          RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getTimeoutMs());
        requestFactory.setReadTimeout(properties.getTimeoutMs());
        this.restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public String chat(String prompt) throws Exception {
        String body = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(properties.getApiKey()))
                .body(Map.of(
                        "model", properties.getModel(),
                        "messages", new Object[]{
                                Map.of("role", "system", "content", "You extract todo fields and reply with JSON only."),
                                Map.of("role", "user", "content", prompt)
                        },
                        "temperature", 0.1
                ))
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(body);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.isNull()) {
            throw new IllegalStateException("DeepSeek response missing message content");
        }
        return contentNode.asText();
    }
}
