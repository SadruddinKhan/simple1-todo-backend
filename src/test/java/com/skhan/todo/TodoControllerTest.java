package com.skhan.todo;

import com.skhan.todo.controller.TodoController;
import com.skhan.todo.dto.TodoRequest;
import com.skhan.todo.exception.GlobalExceptionHandler;
import com.skhan.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TodoControllerTest {

    private MockMvc mockMvc;
    private TodoService todoService;

    @BeforeEach
    void setUp() {
        todoService = new TodoService();
        todoService.deleteAllTodos();
        todoService.createTodo(new TodoRequest("Task 1", "Description 1", false));
        todoService.createTodo(new TodoRequest("Task 2", "Description 2", true));

        TodoController todoController = new TodoController(todoService);
        mockMvc = MockMvcBuilders.standaloneSetup(todoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldGetAllTodos() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Task 1")))
                .andExpect(jsonPath("$[1].title", is("Task 2")));
    }

    @Test
    void shouldFilterTodosByCompletedStatus() throws Exception {
        mockMvc.perform(get("/api/todos").param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Task 2")));

        mockMvc.perform(get("/api/todos").param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Task 1")));
    }

    @Test
    void shouldSearchTodosByKeyword() throws Exception {
        mockMvc.perform(get("/api/todos").param("search", "Task 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Task 1")));
    }

    @Test
    void shouldSearchTodosViaDedicatedSearchEndpoint() throws Exception {
        mockMvc.perform(get("/api/todos/search").param("query", "Task 2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Task 2")));

        mockMvc.perform(get("/api/todos/search").param("q", "Description 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Task 1")));

        mockMvc.perform(get("/api/todos/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetTodoById() throws Exception {
        Long firstId = todoService.getAllTodos(null, null).get(0).getId();

        mockMvc.perform(get("/api/todos/" + firstId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstId.intValue())))
                .andExpect(jsonPath("$.title", is("Task 1")));
    }

    @Test
    void shouldReturn404WhenTodoNotFound() throws Exception {
        mockMvc.perform(get("/api/todos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Todo not found with id: 999999")));
    }

    @Test
    void shouldCreateTodo() throws Exception {
        String jsonPayload = """
                {
                    "title": "New Task",
                    "description": "New Description",
                    "completed": false
                }
                """;

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("New Task")))
                .andExpect(jsonPath("$.description", is("New Description")))
                .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    void shouldReturn400WhenCreatingTodoWithEmptyTitle() throws Exception {
        String jsonPayload = """
                {
                    "title": "",
                    "description": "Some description",
                    "completed": false
                }
                """;

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Todo title cannot be empty")));
    }

    @Test
    void shouldUpdateTodo() throws Exception {
        Long firstId = todoService.getAllTodos(null, null).get(0).getId();
        String jsonPayload = """
                {
                    "title": "Updated Task Title",
                    "description": "Updated Description",
                    "completed": true
                }
                """;

        mockMvc.perform(put("/api/todos/" + firstId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstId.intValue())))
                .andExpect(jsonPath("$.title", is("Updated Task Title")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    void shouldToggleTodoStatus() throws Exception {
        Long firstId = todoService.getAllTodos(null, null).get(0).getId();

        mockMvc.perform(patch("/api/todos/" + firstId + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstId.intValue())))
                .andExpect(jsonPath("$.completed", is(true)));

        mockMvc.perform(patch("/api/todos/" + firstId + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    void shouldDeleteTodo() throws Exception {
        Long firstId = todoService.getAllTodos(null, null).get(0).getId();

        mockMvc.perform(delete("/api/todos/" + firstId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/todos/" + firstId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetStats() throws Exception {
        mockMvc.perform(get("/api/todos/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.completed", is(1)))
                .andExpect(jsonPath("$.pending", is(1)));
    }
}
