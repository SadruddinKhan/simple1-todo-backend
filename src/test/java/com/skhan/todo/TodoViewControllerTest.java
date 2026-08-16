package com.skhan.todo;

import com.skhan.todo.controller.TodoViewController;
import com.skhan.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TodoViewControllerTest {

    private MockMvc mockMvc;
    private TodoService todoService;

    @BeforeEach
    void setUp() {
        todoService = new TodoService();
        todoService.init();
        TodoViewController todoViewController = new TodoViewController(todoService);
        mockMvc = MockMvcBuilders.standaloneSetup(todoViewController).build();
    }

    @Test
    void shouldReturnIndexViewWithModelAttributesOnRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("todos"))
                .andExpect(model().attributeExists("totalCount"))
                .andExpect(model().attributeExists("completedCount"))
                .andExpect(model().attributeExists("pendingCount"))
                .andExpect(model().attributeExists("progressPercent"))
                .andExpect(model().attribute("totalCount", is(5L)))
                .andExpect(model().attribute("completedCount", is(2L)))
                .andExpect(model().attribute("pendingCount", is(3L)))
                .andExpect(model().attribute("progressPercent", is(40)));
    }

    @Test
    void shouldReturnIndexViewOnTodosEndpoint() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("todos"));
    }
}
