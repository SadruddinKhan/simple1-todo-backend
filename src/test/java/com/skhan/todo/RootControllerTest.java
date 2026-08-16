package com.skhan.todo;

import com.skhan.todo.controller.RootController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RootControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RootController rootController = new RootController();
        mockMvc = MockMvcBuilders.standaloneSetup(rootController).build();
    }

    @Test
    void shouldReturnOkStatusOnApi() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.message", containsString("Todo Backend API is running successfully")))
                .andExpect(jsonPath("$.version", is("1.0.0")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.endpoints.ui", is("GET /")))
                .andExpect(jsonPath("$.endpoints.getAllTodos", is("GET /api/todos")))
                .andExpect(jsonPath("$.endpoints.searchTodos", is("GET /api/todos/search?query={keyword}")));
    }

    @Test
    void shouldReturnOkStatusOnHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")));
    }
}
