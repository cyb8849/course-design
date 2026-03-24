package com.lotus.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * AI服务实现类
 * 用于与国内AI服务进行交互
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl {

    @Value("${ai.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.base-url:https://api.deepseek.com/v1}")
    private String deepseekBaseUrl;

    @Value("${ai.doubao.api-key:}")
    private String doubaoApiKey;

    @Value("${ai.doubao.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String doubaoBaseUrl;

    private final RestTemplate restTemplate;

    /**
     * 调用DeepSeek API进行问答
     * @param question 问题
     * @return 回答
     */
    public String deepseekChat(String question) {
        if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
            return "DeepSeek API key is not configured";
        }

        try {
            String url = deepseekBaseUrl + "/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepseekApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("messages", new Object[]{
                    new HashMap<String, Object>() {{ put("role", "user"); put("content", question); }}
            });
            requestBody.put("temperature", 0.7);

            HttpEntity<String> requestEntity = new HttpEntity<>(JSON.toJSONString(requestBody), headers);
            String response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();

            JSONObject responseJson = JSON.parseObject(response);
            if (responseJson.containsKey("choices")) {
                return responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            }
            return "No response from DeepSeek";
        } catch (Exception e) {
            log.error("DeepSeek API call failed: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 调用豆包API进行问答
     * @param question 问题
     * @return 回答
     */
    public String doubaoChat(String question) {
        if (doubaoApiKey == null || doubaoApiKey.isEmpty()) {
            return "Doubao API key is not configured";
        }

        try {
            String url = doubaoBaseUrl + "/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + doubaoApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "doubao-seed-2-0-pro-260215");
            requestBody.put("messages", new Object[]{
                    new HashMap<String, Object>() {{ put("role", "user"); put("content", question); }}
            });
            requestBody.put("temperature", 0.7);

            HttpEntity<String> requestEntity = new HttpEntity<>(JSON.toJSONString(requestBody), headers);
            String response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();

            JSONObject responseJson = JSON.parseObject(response);
            if (responseJson.containsKey("choices")) {
                return responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            }
            return "No response from Doubao";
        } catch (Exception e) {
            log.error("Doubao API call failed: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 调用豆包API进行流式问答
     * @param question 问题
     * @return 流式回答的回调
     */
    public void doubaoStreamChat(String question, java.util.function.Consumer<String> callback) {
        if (doubaoApiKey == null || doubaoApiKey.isEmpty()) {
            callback.accept("Doubao API key is not configured");
            return;
        }

        try {
            URL url = new URL(doubaoBaseUrl + "/chat/completions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + doubaoApiKey);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "doubao-seed-2-0-pro-260215");
            requestBody.put("messages", new Object[]{
                    new HashMap<String, Object>() {{ put("role", "user"); put("content", question); }}
            });
            requestBody.put("temperature", 0.7);
            requestBody.put("stream", true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = JSON.toJSONString(requestBody).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if (data.equals("[DONE]")) {
                            break;
                        }
                        try {
                            JSONObject chunk = JSON.parseObject(data);
                            if (chunk.containsKey("choices")) {
                                JSONObject choice = chunk.getJSONArray("choices").getJSONObject(0);
                                if (choice.containsKey("delta")) {
                                    JSONObject delta = choice.getJSONObject("delta");
                                    if (delta.containsKey("content")) {
                                        String content = delta.getString("content");
                                        if (!content.isEmpty()) {
                                            callback.accept(content);
                                            // 模拟延迟，让流式效果更明显
                                            Thread.sleep(50);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error parsing streaming chunk: {}", e.getMessage());
                        }
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            log.error("Doubao streaming API call failed: {}", e.getMessage());
            callback.accept("Error: " + e.getMessage());
        }
    }

    /**
     * 智能问答（默认使用豆包）
     * @param question 问题
     * @return 回答
     */
    public String chat(String question) {
        log.info("AI chat request: {}", question);
        // 优先使用豆包
        if (doubaoApiKey != null && !doubaoApiKey.isEmpty()) {
            log.info("Using Doubao AI service");
            String doubaoResponse = doubaoChat(question);
            log.info("Doubao response: {}", doubaoResponse);
            // 如果豆包返回错误信息，尝试使用DeepSeek
            if (doubaoResponse.startsWith("Error:") && deepseekApiKey != null && !deepseekApiKey.isEmpty()) {
                log.error("Doubao API call failed, trying DeepSeek: {}", doubaoResponse);
                return deepseekChat(question);
            }
            return doubaoResponse;
        }
        // 其次使用DeepSeek
        if (deepseekApiKey != null && !deepseekApiKey.isEmpty()) {
            log.info("Using DeepSeek AI service");
            return deepseekChat(question);
        }
        // 如果都没有配置，返回提示信息
        log.warn("AI service is not configured");
        return "AI service is not configured. Please set up Doubao or DeepSeek API key.";
    }
}
