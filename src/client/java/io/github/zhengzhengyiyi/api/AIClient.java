package io.github.zhengzhengyiyi.api;

import java.net.*;
import java.net.http.*;
import java.util.concurrent.*;

public class AIClient {
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";
    private final HttpClient httpClient;
    
    public AIClient() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    public CompletableFuture<String> sendChatRequest(String model, String message) {
        String requestBody = String.format(
            "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"stream\": false}",
            model, escapeJson(message)
        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OLLAMA_BASE_URL + "/api/chat"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    return extractContentFromResponse(response.body());
                } else {
                    throw new RuntimeException("request failed" + response.statusCode() + " - " + response.body());
                }
            });
    }
    
    private String extractContentFromResponse(String jsonResponse) {
        try {
            int contentIndex = jsonResponse.indexOf("\"content\":\"");
            if (contentIndex == -1) return "content invalid";
            
            contentIndex += 11;
            int endIndex = jsonResponse.indexOf("\"", contentIndex);
            return jsonResponse.substring(contentIndex, endIndex).replace("\\n", "\n");
        } catch (Exception e) {
            return "error";
        }
    }
    
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    public CompletableFuture<Boolean> checkServerStatus() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OLLAMA_BASE_URL + "/api/tags"))
            .GET()
            .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> response.statusCode() == 200)
            .exceptionally(ex -> false);
    }
}
