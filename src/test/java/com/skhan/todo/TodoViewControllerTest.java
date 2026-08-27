package com.skhan.todo;

import com.skhan.todo.controller.TodoViewController;
import com.skhan.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.servlet.view.InternalResourceViewResolver;

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

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(todoViewController)
                .setViewResolvers(viewResolver)
                .build();
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

    @Test
    void shouldReturnAboutViewWithModelAttributes() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"))
                .andExpect(model().attributeExists("totalCount"))
                .andExpect(model().attributeExists("completedCount"))
                .andExpect(model().attributeExists("pendingCount"))
                .andExpect(model().attributeExists("progressPercent"))
                .andExpect(model().attributeExists("version"))
                .andExpect(model().attributeExists("javaVersion"))
                .andExpect(model().attributeExists("springBootVersion"))
                .andExpect(model().attribute("totalCount", is(5L)))
                .andExpect(model().attribute("completedCount", is(2L)))
                .andExpect(model().attribute("pendingCount", is(3L)))
                .andExpect(model().attribute("progressPercent", is(40)))
                .andExpect(model().attribute("version", is("1.0.0")))
                .andExpect(model().attribute("springBootVersion", is("4.1.0")));
    }
}
