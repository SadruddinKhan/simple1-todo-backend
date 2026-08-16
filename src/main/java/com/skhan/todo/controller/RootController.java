package com.skhan.todo.controller;

import com.skhan.todo.dto.ApiStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class RootController {

    @GetMapping({"/api", "/health"})
    public ResponseEntity<ApiStatusResponse> getApiStatus() {
        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("ui", "GET /");
        endpoints.put("status", "GET /api");
        endpoints.put("getAllTodos", "GET /api/todos");
        endpoints.put("searchTodos", "GET /api/todos/search?query={keyword}");
        endpoints.put("getTodoById", "GET /api/todos/{id}");
        endpoints.put("getTodoStats", "GET /api/todos/stats");
        endpoints.put("createTodo", "POST /api/todos");
        endpoints.put("updateTodo", "PUT /api/todos/{id}");
        endpoints.put("toggleTodoStatus", "PATCH /api/todos/{id}/toggle");
        endpoints.put("deleteTodo", "DELETE /api/todos/{id}");
        endpoints.put("deleteAllTodos", "DELETE /api/todos");

        ApiStatusResponse response = new ApiStatusResponse(
                "OK",
                "Todo Backend API is running successfully",
                "1.0.0",
                endpoints
        );

        return ResponseEntity.ok(response);
    }
}
