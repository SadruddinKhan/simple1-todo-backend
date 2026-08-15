package com.skhan.todo.controller;

import com.skhan.todo.dto.TodoRequest;
import com.skhan.todo.dto.TodoStatsResponse;
import com.skhan.todo.model.Todo;
import com.skhan.todo.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "*")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * Get all todos with optional filtering by completed status or keyword search.
     * Example: GET /api/todos?completed=true&search=spring
     */
    @GetMapping
    public ResponseEntity<List<Todo>> getAllTodos(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String search) {
        List<Todo> todos = todoService.getAllTodos(completed, search);
        return ResponseEntity.ok(todos);
    }

    /**
     * Search todos by title or description keyword.
     * Example: GET /api/todos/search?query=spring or GET /api/todos/search?q=spring
     */
    @GetMapping("/search")
    public ResponseEntity<List<Todo>> searchTodos(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String q) {
        String searchTerm = query != null ? query : q;
        List<Todo> todos = todoService.searchTodos(searchTerm);
        return ResponseEntity.ok(todos);
    }

    /**
     * Get summary counts of total, completed, and pending todos.
     * Example: GET /api/todos/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<TodoStatsResponse> getTodoStats() {
        TodoStatsResponse stats = new TodoStatsResponse(
                todoService.getTotalCount(),
                todoService.getCompletedCount(),
                todoService.getPendingCount()
        );
        return ResponseEntity.ok(stats);
    }

    /**
     * Get a single todo by its ID.
     * Example: GET /api/todos/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable Long id) {
        Todo todo = todoService.getTodoById(id);
        return ResponseEntity.ok(todo);
    }

    /**
     * Create a new todo item.
     * Example: POST /api/todos
     */
    @PostMapping
    public ResponseEntity<Todo> createTodo(@RequestBody TodoRequest request) {
        Todo createdTodo = todoService.createTodo(request);
        return new ResponseEntity<>(createdTodo, HttpStatus.CREATED);
    }

    /**
     * Update an existing todo item.
     * Example: PUT /api/todos/1
     */
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, @RequestBody TodoRequest request) {
        Todo updatedTodo = todoService.updateTodo(id, request);
        return ResponseEntity.ok(updatedTodo);
    }

    /**
     * Toggle the completion status of a todo.
     * Example: PATCH /api/todos/1/toggle
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Todo> toggleTodoStatus(@PathVariable Long id) {
        Todo updatedTodo = todoService.toggleTodoStatus(id);
        return ResponseEntity.ok(updatedTodo);
    }

    /**
     * Delete a single todo by ID.
     * Example: DELETE /api/todos/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete all todos.
     * Example: DELETE /api/todos
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllTodos() {
        todoService.deleteAllTodos();
        return ResponseEntity.noContent().build();
    }
}
