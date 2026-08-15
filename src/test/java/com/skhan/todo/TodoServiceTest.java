package com.skhan.todo;

import com.skhan.todo.dto.TodoRequest;
import com.skhan.todo.exception.ResourceNotFoundException;
import com.skhan.todo.model.Todo;
import com.skhan.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TodoServiceTest {

    private TodoService todoService;

    @BeforeEach
    void setUp() {
        todoService = new TodoService();
        todoService.deleteAllTodos();
    }

    @Test
    void testCreateAndGetTodo() {
        Todo created = todoService.createTodo(new TodoRequest("Write code", "In-memory list service", false));
        assertNotNull(created.getId());
        assertEquals("Write code", created.getTitle());
        assertEquals("In-memory list service", created.getDescription());
        assertFalse(created.isCompleted());
        assertNotNull(created.getCreatedAt());

        Todo found = todoService.getTodoById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void testGetAllTodosWithFilter() {
        todoService.createTodo(new TodoRequest("Task A", "Desc A", false));
        todoService.createTodo(new TodoRequest("Task B", "Desc B", true));
        todoService.createTodo(new TodoRequest("Special Task C", "Desc C", false));

        List<Todo> all = todoService.getAllTodos(null, null);
        assertEquals(3, all.size());

        List<Todo> completed = todoService.getAllTodos(true, null);
        assertEquals(1, completed.size());
        assertEquals("Task B", completed.get(0).getTitle());

        List<Todo> pending = todoService.getAllTodos(false, null);
        assertEquals(2, pending.size());

        List<Todo> searchResults = todoService.getAllTodos(null, "Special");
        assertEquals(1, searchResults.size());
        assertEquals("Special Task C", searchResults.get(0).getTitle());
    }

    @Test
    void testSearchTodos() {
        todoService.createTodo(new TodoRequest("Learn Spring Boot", "Understand dependency injection", false));
        todoService.createTodo(new TodoRequest("Write Dockerfile", "Containerize the Spring Boot app", true));
        todoService.createTodo(new TodoRequest("Deploy to AWS", "Setup EC2 instance", false));

        // Search matching title
        List<Todo> results1 = todoService.searchTodos("spring");
        assertEquals(2, results1.size());

        // Search matching description
        List<Todo> results2 = todoService.searchTodos("containerize");
        assertEquals(1, results2.size());
        assertEquals("Write Dockerfile", results2.get(0).getTitle());

        // Search with no matching term
        List<Todo> results3 = todoService.searchTodos("Kubernetes");
        assertEquals(0, results3.size());

        // Null or blank query returns all todos
        assertEquals(3, todoService.searchTodos(null).size());
        assertEquals(3, todoService.searchTodos("   ").size());
    }

    @Test
    void testUpdateTodo() {
        Todo created = todoService.createTodo(new TodoRequest("Original", "Original Desc", false));
        Todo updated = todoService.updateTodo(created.getId(), new TodoRequest("Updated", "Updated Desc", true));

        assertEquals("Updated", updated.getTitle());
        assertEquals("Updated Desc", updated.getDescription());
        assertTrue(updated.isCompleted());
    }

    @Test
    void testToggleTodoStatus() {
        Todo created = todoService.createTodo(new TodoRequest("Toggle Test", "Desc", false));
        assertFalse(created.isCompleted());

        Todo toggled = todoService.toggleTodoStatus(created.getId());
        assertTrue(toggled.isCompleted());

        Todo toggledBack = todoService.toggleTodoStatus(created.getId());
        assertFalse(toggledBack.isCompleted());
    }

    @Test
    void testDeleteTodo() {
        Todo created = todoService.createTodo(new TodoRequest("To Delete", "Desc", false));
        assertEquals(1, todoService.getTotalCount());

        todoService.deleteTodo(created.getId());
        assertEquals(0, todoService.getTotalCount());

        assertThrows(ResourceNotFoundException.class, () -> todoService.getTodoById(created.getId()));
    }

    @Test
    void testValidationOnEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> todoService.createTodo(new TodoRequest("", "Desc", false)));
        assertThrows(IllegalArgumentException.class, () -> todoService.createTodo(new TodoRequest("   ", "Desc", false)));
        assertThrows(IllegalArgumentException.class, () -> todoService.createTodo(null));
    }
}
