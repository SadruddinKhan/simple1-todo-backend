package com.skhan.todo.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ApiStatusResponse {
    private String status;
    private String message;
    private String version;
    private LocalDateTime timestamp;
    private Map<String, String> endpoints;

    public ApiStatusResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiStatusResponse(String status, String message, String version, Map<String, String> endpoints) {
        this.status = status;
        this.message = message;
        this.version = version;
        this.timestamp = LocalDateTime.now();
        this.endpoints = endpoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, String> endpoints) {
        this.endpoints = endpoints;
    }
}
