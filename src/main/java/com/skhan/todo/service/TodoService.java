package com.skhan.todo.service;

import com.skhan.todo.dto.TodoRequest;
import com.skhan.todo.exception.ResourceNotFoundException;
import com.skhan.todo.model.Todo;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TodoService {

    // In-memory list to manage todos without a database
    private final List<Todo> todoList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    @PostConstruct
    public void init() {
        loadDefaultData();
    }

    /**
     * Seeds initial dummy todos so the application starts with pre-populated data.
     */
    public void loadDefaultData() {
        createTodo(new TodoRequest(
                "Learn Spring Boot Fundamentals",
                "Understand Spring Boot architecture, auto-configuration, and dependency injection",
                true
        ));
        createTodo(new TodoRequest(
                "Build Todo REST API",
                "Implement full CRUD operations using in-memory List in service layer without database",
                true
        ));
        createTodo(new TodoRequest(
                "Setup CI/CD Pipeline",
                "Configure automated GitHub Actions workflow for building, testing, and packaging",
                false
        ));
        createTodo(new TodoRequest(
                "Dockerize Backend Application",
                "Create a multi-stage Dockerfile and test running container on port 8080",
                false
        ));
        createTodo(new TodoRequest(
                "Integrate Frontend Client",
                "Connect React or Angular frontend to backend REST endpoints and test CORS",
                false
        ));
    }

    public List<Todo> getAllTodos(Boolean completed, String search) {
        return todoList.stream()
                .filter(todo -> completed == null || todo.isCompleted() == completed)
                .filter(todo -> {
                    if (search == null || search.trim().isEmpty()) {
                        return true;
                    }
                    String term = search.toLowerCase();
                    boolean matchTitle = todo.getTitle() != null && todo.getTitle().toLowerCase().contains(term);
                    boolean matchDesc = todo.getDescription() != null && todo.getDescription().toLowerCase().contains(term);
                    return matchTitle || matchDesc;
                })
                .collect(Collectors.toList());
    }

    public Todo getTodoById(Long id) {
        return todoList.stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
    }

    public Todo createTodo(TodoRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Todo title cannot be empty");
        }

        long newId = idCounter.incrementAndGet();
        boolean isCompleted = request.getCompleted() != null && request.getCompleted();

        Todo newTodo = new Todo(
                newId,
                request.getTitle().trim(),
                request.getDescription() != null ? request.getDescription().trim() : "",
                isCompleted
        );

        todoList.add(newTodo);
        return newTodo;
    }

    public Todo updateTodo(Long id, TodoRequest request) {
        Todo existingTodo = getTodoById(id);

        if (request.getTitle() != null) {
            if (request.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Todo title cannot be empty");
            }
            existingTodo.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            existingTodo.setDescription(request.getDescription().trim());
        }

        if (request.getCompleted() != null) {
            existingTodo.setCompleted(request.getCompleted());
        }

        existingTodo.setUpdatedAt(LocalDateTime.now());
        return existingTodo;
    }

    public Todo toggleTodoStatus(Long id) {
        Todo existingTodo = getTodoById(id);
        existingTodo.setCompleted(!existingTodo.isCompleted());
        existingTodo.setUpdatedAt(LocalDateTime.now());
        return existingTodo;
    }

    public void deleteTodo(Long id) {
        Todo existingTodo = getTodoById(id);
        todoList.remove(existingTodo);
    }

    public void deleteAllTodos() {
        todoList.clear();
    }

    public long getTotalCount() {
        return todoList.size();
    }

    public long getCompletedCount() {
        return todoList.stream().filter(Todo::isCompleted).count();
    }

    public long getPendingCount() {
        return todoList.stream().filter(todo -> !todo.isCompleted()).count();
    }
}
